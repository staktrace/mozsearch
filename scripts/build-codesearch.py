#!/usr/bin/env python3

from __future__ import absolute_import
import os
import sys
import json
import shutil

from lib import run

def copy_objdir_files(dest_dir, config):
    for d in open(os.path.join(config['index_path'], 'objdir-dirs')).readlines():
        d = d.strip()
        run(['mkdir', '-p', os.path.join(dest_dir, d)])

    paths = open(os.path.join(config['index_path'], 'objdir-files')).readlines()
    for path in paths:
        path = path.strip()
        source = path.replace('__GENERATED__', config['objdir_path'])
        try:
            data = open(source).read()
        except:
            continue

        dest = os.path.join(dest_dir, path)
        f = open(dest, 'w')
        f.write(data)
        f.close()

# Make sure a failure during a prior invocation of this command does not break
# our operation.  This step is not designed to run concurrently, so this is ok.
if os.path.exists('/tmp/dummy'):
    shutil.rmtree('/tmp/dummy')
os.mkdir('/tmp/dummy')

config_fname = sys.argv[1]
tree_name = sys.argv[2]

livegrep_config = {
    'name': 'Searchfox',
    'repositories': [],
    'fs_paths': [],
}

config = json.load(open(config_fname))
tree = config['trees'][tree_name]

if 'git_path' in tree:
    run(['ln', '-s', tree['git_path'], '/tmp/dummy/%s' % tree_name])

    livegrep_config['repositories'].append({
        'name': tree_name,
        'path': tree['git_path'],
        'revisions': ['HEAD'],
        'walk_submodules': tree.get('walk_submodules', True)
    })

    # comm-central has a mozilla subfolder which is another git repo, so
    # add that to the livegrep config as well
    if tree_name == 'comm-central':
        livegrep_config['repositories'].append({
            'name': 'mozilla-subrepo',
            'path': tree['files_path'] + '/mozilla/',
            'revisions': ['HEAD']
        })
else:
    run(['ln', '-s', tree['files_path'], '/tmp/dummy/%s' % tree_name])

    # If we don't include the trailing '/', then all search
    # results will include an initial slash in their paths.
    livegrep_config['fs_paths'].append({
        'name': tree_name,
        'path': tree['files_path'] + '/'
    })

tmp_objdir = '/tmp/dummy/objdir-%s' % tree_name
os.mkdir(tmp_objdir)
copy_objdir_files(tmp_objdir, tree)

livegrep_config['fs_paths'].append({
    'name': tree_name + '-__GENERATED__',
    'path': 'objdir-%s/' % tree_name,
})

json.dump(livegrep_config, open('/tmp/livegrep.json', 'w'))

run(['codesearch', '/tmp/livegrep.json',
     '-dump_index', tree['codesearch_path'],
     '-index_only',
     '-max_matches', '1000',
     # the default is 27 which is a chunk size of 128 MiB which was resulting in
     # only 2 threads having work to do for very big queries, so we're scaling
     # down by 8 (2**3) to be able to saturate 8 threads and give each thread
     # potentially more than 1 work unit.
     #
     # (Although 2 work units probably isn't optimal for useful work stealing,
     # but I don't quite understand enough about any scale benefits the chunks
     # might have like clever SQLite style delta-encoding, so I want to avoid
     # making too dramatic a change.  But in theory a value of 22 might be
     # better to give each thread potentialy 8 work units where we currently
     # only see 1.)
     '-chunk_power', '24',
     '-line_limit', '4096'], stdin=open('/dev/null'), cwd='/tmp/dummy')

run(['rm', '-rf', '/tmp/dummy'])
run(['rm', '-rf', '/tmp/livegrep.json'])
