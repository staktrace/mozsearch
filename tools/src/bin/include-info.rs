extern crate env_logger;
#[macro_use]
extern crate log;

use std::collections::{HashMap, HashSet};
use std::env;
use std::fs::{File, read_dir};
use std::io::{BufRead, BufReader};
use std::path::{Path, PathBuf};

fn read_files_recursively(basepath: &Path, path: &Path, map: &mut HashMap<String, Vec<String>>) {
    if path.is_dir() {
        debug!("Recursing into {:?}", path);
        for entry in read_dir(path).unwrap() {
            let entry = entry.unwrap();
            let subpath = entry.path();
            read_files_recursively(basepath, &subpath, map);
        }
        return;
    }

    let includer = path.strip_prefix(basepath).unwrap().to_string_lossy();
    debug!("Reading {:?}", includer);
    let file = File::open(path).unwrap();
    let reader = BufReader::new(file);
    for line in reader.lines() {
        let included = line.unwrap();
        map.entry(included).or_insert_with(Vec::new).push(includer.to_string());
    }
}

fn compute_recursive_deps(include_graph: &HashMap<String, Vec<String>>, start: &str) -> HashSet<String> {
    let mut dependencies = HashSet::<String>::new();
    dependencies.insert(start.to_string());

    let mut keep_going = true;
    while keep_going {
        let mut more_dependencies = HashSet::<String>::new();
        for dep in &dependencies {
            if let Some(dep_deps) = include_graph.get(dep) {
                for dep_dep in dep_deps {
                    if dependencies.contains(dep_dep) {
                        continue;
                    }
                    more_dependencies.insert(dep_dep.to_string());
                }
            }
        }
        keep_going = !more_dependencies.is_empty();
        dependencies.extend(more_dependencies);
    }

    dependencies
}

fn main() {
    env_logger::from_env(env_logger::Env::default().default_filter_or("info")).init();

    let args: Vec<_> = env::args().collect();
    let info_dir = PathBuf::from(&args[1]);

    let mut include_graph = HashMap::new();
    read_files_recursively(&info_dir, &info_dir, &mut include_graph);

    if args.len() > 2 {
        for arg in &args[2..] {
            let dependencies = compute_recursive_deps(&include_graph, arg);
            println!("{} has {} dependencies: {:?}", arg, dependencies.len(), dependencies);
        }
    } else {
        for included in include_graph.keys() {
            let dependencies = compute_recursive_deps(&include_graph, included);
            println!("{},{}", included, dependencies.len());
        }
    }
}
