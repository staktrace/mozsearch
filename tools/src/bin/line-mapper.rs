extern crate bcmp;
extern crate env_logger;
#[macro_use]
extern crate log;
extern crate tools;

use std::env;
use std::fs;
use std::path::PathBuf;
use tools::config;
use tools::git_ops;

fn main() {
    env_logger::init();

    let cfg_file = env::args().nth(1).unwrap();
    let tree = env::args().nth(2).unwrap();
    let dst_root = env::args().nth(3).unwrap();

    info!("Processing tree {} for skip-blame revisions...", tree);

    let cfg = config::load(&cfg_file, true);
    let tree = cfg.trees.get(&tree).unwrap();
    if let Some(git_data) = &tree.git {
        for rev in git_data.revs_to_ignore() {
            let mut rev_path = PathBuf::from(&dst_root);
            rev_path.push(rev);

            if rev_path.is_dir() {
                debug!("Data for rev {} exists, skipping...", rev);
                continue;
            }

            info!("Generating data for rev {}...", rev);
            let commit = git_data.repo.revparse_single(rev)
                                      .unwrap()
                                      .into_commit()
                                      .unwrap();
            if commit.parent_ids().len() != 1 {
                warn!("Revision {} does not have a unique parent, cannot diff against parent!", rev);
                continue;
            }

            let parent_commit = commit.parent(0).unwrap();
            let _diff = git_ops::diff_trees(git_data, &commit, &parent_commit).unwrap();

            fs::create_dir_all(rev_path).unwrap();
        }
    }
}
