#!/usr/bin/env bash

set -x # Show commands
set -eu # Errors/undefined vars are fatal
set -o pipefail # Check all commands in a pipeline

# Install zlib.h (needed for NSS build)
sudo apt-get install -y zlib1g-dev

# Install python2 and six (needed for cinnabar and idl-analyze.py)
sudo apt-get install -y python2.7 python-six

# Building LLVM likes to have ninja; pernosco also can use it if we ever index that.
sudo apt-get install -y ninja-build

# cargo-insta makes it possible to use the UI documented at
# https://insta.rs/docs/cli/ to review changes to "check" scripts.  For the test
# repo, this is used by `make review-test-repo`.  It's not expected that this
# will actually be necessary on the production indexer and so this isn't part of
# the update process.
cargo install cargo-insta

# To help install node.js and similar, we install rtx-cli, a rust-based "asdf"
# alternative, which if you don't know what "asdf" is, but know what "nvm" is,
# it's basically a super-nvm for multiple languages, etc.  We use the install
# method documented at https://github.com/jdxcode/rtx#cargo but there are a
# bunch of other options.
#
# The core rationale here is that I've locally been using "nvm" for node.js
# purposes for a while now and it's been a much better experience than trying to
# use debian/ubuntu distro-provided versions of node, and in particular can be
# invaluable when trying to just get things to work when packages are involved
# that may involve native modules/libraries which can make it hard to uniformly
# use the latest revision.  I'm somewhat hopeful that
cargo install rtx-cli

# Install node.js v18 for scip-typescript
rtx install nodejs@18

# Install scip-typescript under node.js v18
rtx exec nodejs@18 -- npm install -g @sourcegraph/scip-typescript

# Install scip-python under node.js v18 as well
rtx exec nodejs@18 -- npm install -g @sourcegraph/scip-python

# Create update script.
cat > update.sh <<"THEEND"
#!/usr/bin/env bash

set -x # Show commands
set -eu # Errors/undefined vars are fatal
set -o pipefail # Check all commands in a pipeline

exec &> update-log

date

if [ $# != 3 ]
then
    echo "usage: $0 <branch> <mozsearch-repo> <config-repo>"
    exit 1
fi

BRANCH=$1
MOZSEARCH_REPO=$2
CONFIG_REPO=$3

echo Branch is $BRANCH
echo Mozsearch repository is $MOZSEARCH_REPO
echo Config repository is $CONFIG_REPO

# Install mozsearch.
rm -rf mozsearch
git clone -b $BRANCH $MOZSEARCH_REPO mozsearch
pushd mozsearch
git submodule init
git submodule update
popd

# Install files from the config repo.
rm -rf config
git clone $CONFIG_REPO config
pushd config
git checkout $BRANCH -- || true
popd

date

# Let mozsearch tell us what commonly changing dependencies to install plus
# perform any build steps.
mozsearch/infrastructure/indexer-update.sh

date
THEEND

chmod +x update.sh

# Run the update script for a side effect of downloading the crates.io
# dependencies ahead of time since we're seeing intermittent network problems
# downloading crates in https://bugzilla.mozilla.org/show_bug.cgi?id=1720037.
#
# Note that because the update script fully deletes the mozsearch directory,
# this really is just:
# - Validating the image can compile and use rust and clang correctly.
# - Caching some crates in `~/.cargo`.
./update.sh master https://github.com/mozsearch/mozsearch https://github.com/mozsearch/mozsearch-mozilla
mv update-log provision-update-log-1

# Run this a second time to make sure the script is actually idempotent, so we
# don't have any surprises when the update script gets run when the VM spins up.
./update.sh master https://github.com/mozsearch/mozsearch https://github.com/mozsearch/mozsearch-mozilla
mv update-log provision-update-log-2
