let treeRoot = scriptArgs[0];
let indexRoot = scriptArgs[1];
let mozSearchRoot = scriptArgs[2];
let filenames = scriptArgs.slice(3);

let window = this;
run(mozSearchRoot + "/highlight.pack.js");

run(mozSearchRoot + "/output.js");

function parseAnalysis(line)
{
  let parts = line.split(" ");
  if (parts.length != 4 && parts.length != 5) {
    throw `Invalid analysis line: ${line}`;
  }

  let [linenum, colnum] = parts[0].split(":");
  return {line: parseInt(linenum), col: parseInt(colnum),
          kind: parts[1], name: parts[2], id: parts[3], extra: parts[4]};
}

let jumps = snarf(indexRoot + "/jumps");
let jumpLines = jumps.split("\n");
jumps = new Set();
for (let id of jumpLines) {
  jumps.add(id);
}

function chooseLanguage(filename)
{
  let suffix = getSuffix(filename);

  let exclude = {'ogg': true, 'ttf': true, 'xpi': true, 'png': true, 'bcmap': true,
                 'gif': true, 'ogv': true, 'jpg': true, 'bmp': true, 'icns': true, 'ico': true,
                 'mp4': true, 'sqlite': true, 'jar': true, 'webm': true, 'woff': true,
                 'class': true, 'm4s': true, 'mgif': true, 'wav': true, 'opus': true,
                 'mp3': true, 'otf': true};
  if (suffix in exclude) {
    return "skip";
  }

  let table = {'js': 'javascript', 'jsm': 'javascript',
               'cpp': 'cpp', 'h': 'cpp', 'cc': 'cpp', 'hh': 'cpp', 'c': 'cpp',
               'py': 'python', 'sh': 'bash', 'build': 'python', 'ini': 'ini',
               'java': 'java', 'json': 'json', 'xml': 'xml', 'css': 'css',
               'html': 'html'};
  if (suffix in table) {
    return table[suffix];
  }
  return null;
}

function toHTML(code)
{
  code = code.replace("&", "&amp;", "gm");
  code = code.replace("<", "&lt;", "gm");
  return code;
}

function generatePanel()
{
  return `
  <div class="panel">
    <button id="panel-toggle">
      <span class="navpanel-icon expanded" aria-hidden="false"></span>
      Navigation
    </button>
    <section id="panel-content" aria-expanded="true" aria-hidden="false">

      <h4>Mercurial (b8e628af0b5c)</h4>
      <ul>

        <li>
          <a href="https://hg.mozilla.org/mozilla-central/filelog/b8e628af0b5c/js/src/devtools/rootAnalysis/build.js" title="Log" class="log icon">Log</a>
        </li>
        <li>
          <a href="https://hg.mozilla.org/mozilla-central/annotate/b8e628af0b5c/js/src/devtools/rootAnalysis/build.js" title="Blame" class="blame icon">Blame</a>
        </li>
            <li>
              <a href="https://hg.mozilla.org/mozilla-central/diff/b8e628af0b5c/js/src/devtools/rootAnalysis/build.js" title="Diff" class="diff icon">Diff</a>
            </li>
        <li>
          <a href="https://hg.mozilla.org/mozilla-central/raw-file/b8e628af0b5c/js/src/devtools/rootAnalysis/build.js" title="Raw" class="raw icon">Raw</a>
        </li>
      </ul>

    </section>
  </div>
`;
}

function generateFile(filename, opt)
{
  let language = chooseLanguage(filename);

  let code;
  if (language == "skip") {
    code = "binary file";
    language = null;
  } else {
    try {
      code = snarf(treeRoot + filename);
    } catch (e) {
      code = "binary file";
      language = null;
    }
  }

  let analysisLines = [];

  try {
    let analysis = snarf(indexRoot + "/analysis" + filename);
    analysisLines = analysis.split("\n");
    analysisLines.pop();
  } catch (e) {
  }
  analysisLines.push("100000000000:0 eof BAD BAD");

  let lastLine = -1;
  let lastCol = -1;

  let datum = parseAnalysis(analysisLines[0]);
  let analysisIndex = 1;

  if (language) {
    code = hljs.highlight(language, code, true).value;
  } else {
    code = toHTML(code);
  }

  let lines = code.split("\n");

  let content = '';

  function out(s) {
    content += s;
  }

  out(generateBreadcrumbs(filename, opt));
  out(generatePanel());

  out(`
<table id="file" class="file">
  <thead class="visually-hidden">
    <th scope="col">Line</th>
    <th scope="col">Code</th>
  </thead>
  <tbody>
    <tr>
      <td id="line-numbers">
`);

  let lineNum = 1;
  for (let line of lines) {
    out(`<span id="${lineNum}" class="line-number" unselectable="on" rel="#${lineNum}">${lineNum}</span>\n`);
    lineNum++;
  }

  out(`      </td>
      <td class="code">
<pre>`);

  function outputLine(lineNum, line) {
    let col = 0;
    for (let i = 0; i < line.length; i++) {
      let ch = line[i];

      if (ch == '&') {
        do {
          out(line[i]);
          i++;
        } while (line[i] != ';');
        out(line[i]);
        col++;
        continue;
      }

      if (ch == '<') {
        do {
          out(line[i]);
          i++;
        } while (line[i] != '>');
        out(line[i]);
        continue;
      }

      if (lineNum == datum.line && col == datum.col) {
        let extra = "";
        if (datum.extra) {
          extra += `data-extra="${datum.extra}" `;
        }
        if (jumps.has(datum.id)) {
          extra += `data-jump="true" `;
        }
        if (datum.extra && jumps.has(datum.extra)) {
          extra += `data-extra-jump="true" `;
        }
        out(`<span data-id="${datum.id}" data-kind=${datum.kind} ${extra}>${datum.name}</span>`);

        col += datum.name.length - 1;
        i += datum.name.length - 1;
        do {
          datum = parseAnalysis(analysisLines[analysisIndex++]);
        } while (datum.line == lastLine && datum.col == lastCol);
        if (datum.line < lastLine || (datum.line == lastLine && datum.col < lastCol)) {
          throw `Invalid analysis loc: ${filename} ${JSON.stringify(datum)}`;
        }
        lastLine = datum.line;
        lastCol = datum.col;
      } else {
        out(ch);
      }

      col++;
    }
  }

  lineNum = 1;
  for (let line of lines) {
    out(`<code id="line-${lineNum}" aria-labelledby="${lineNum}">`);

    if (lineNum != datum.line) {
      out(line);
    } else {
      outputLine(lineNum, line);
    }

    out(`</code>\n`);

    lineNum++;
  }

  out(`</pre>
      </td>
    </tr>
  </tbody>
</table>
`);

  redirect(indexRoot + "/file" + filename);
  putstr(generate(content, opt));
}

for (let filename of filenames) {
  generateFile(filename, {tree: "mozilla-central"});
}
