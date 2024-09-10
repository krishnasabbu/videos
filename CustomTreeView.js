import React from "react";
import { DiCss3, DiJavascript, DiNpm, DiJava } from "react-icons/di";
import { FaList, FaRegFolder, FaRegFolderOpen, FaFileCode, FaFileAlt } from "react-icons/fa";
import TreeView from "react-accessible-treeview";
import AceEditor from 'react-ace';

import "ace-builds/src-noconflict/mode-java";
import 'ace-builds/src-noconflict/mode-javascript';
import 'ace-builds/src-noconflict/theme-github';
import 'ace-builds/src-noconflict/theme-monokai';

const CustomTreeView = ({ data }) => {
  const [editorContent, setEditorContent] = React.useState(null);

  // Handler for file click
  const onFileSelect = (node) => {
    console.log("Selected node: ", JSON.stringify(node));
    handleFileClick(node.name);
  };

  const handleFileClick = async (name) => {
    try {
      const response = await fetch('http://localhost:8080/micro/content', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          data: name,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const text = await response.text();
      setEditorContent(text);
    } catch (error) {
      console.error('API call failed:', error);
    }
  };

  return (
    <div style={{ display: 'flex', height: '100%' }}>
      {/* TreeView section */}
      <div className="directory" style={{ width: '40%', height:'500px', borderRight: '1px solid #ccc', padding: '10px', overflowY: 'auto' }}>
        <TreeView
          data={data}
          aria-label="directory tree"
          nodeRenderer={({ element, isBranch, isExpanded, getNodeProps, level }) => (
            <div {...getNodeProps()} style={{ paddingLeft: 20 * (level - 1) }}>
              {isBranch ? <FolderIcon isOpen={isExpanded} /> : <FileIcon filename={element.name} />}
              <span onClick={() => onFileSelect(element)}>{element.name}</span>
            </div>
          )}
        />
      </div>

      {/* AceEditor section */}
      <div style={{ width: '60%', paddingLeft: '20px', height: '500px'}}>
        {editorContent ? (
          <AceEditor
            placeholder="Placeholder Text"
            mode="java"
            theme="monokai"
            name="blah2"
            fontSize={16}
            lineHeight={19}
            showPrintMargin={true}
            showGutter={true}
            highlightActiveLine={false}
            value={editorContent}
            style={{ width: '100%' }}
            setOptions={{
            enableBasicAutocompletion: true,
            enableLiveAutocompletion: true,
            enableSnippets: true,
            showLineNumbers: true,
            tabSize: 2,
        }}/>
        ) : ('')
        }
      </div>
    </div>
  );
};

const FolderIcon = ({ isOpen }) =>
  isOpen ? (
    <FaRegFolderOpen color="e8a87c" className="icon" />
  ) : (
    <FaRegFolder color="e8a87c" className="icon" />
  );

const FileIcon = ({ filename }) => {
  const extension = filename.slice(filename.lastIndexOf('.') + 1);

  switch (extension) {
    case 'js':
      return <DiJavascript color="yellow" className="icon" />;
    case 'css':
      return <DiCss3 color="turquoise" className="icon" />;
    case 'json':
      return <FaList color="yellow" className="icon" />;
    case 'npmignore':
      return <DiNpm color="red" className="icon" />;
    case 'java':
      return <DiJava color="blue" className="icon" />;
    case 'xml':
      return <FaFileAlt color="purple" className="icon" />;
    case 'properties':
      return <FaFileAlt color="gray" className="icon" />;
    case 'gradle':
      return <FaFileCode color="green" className="icon" />;
    default:
      return <FaFileCode color="black" className="icon" />;
  }
};

export default CustomTreeView;
