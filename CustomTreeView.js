import React from "react";
import { DiCss3, DiJavascript, DiNpm, DiJava } from "react-icons/di";
import { FaList, FaRegFolder, FaRegFolderOpen, FaFileCode, FaFileAlt } from "react-icons/fa";
import TreeView from "react-accessible-treeview";
import AceEditor from 'react-ace';

import "ace-builds/src-noconflict/mode-java";
import 'ace-builds/src-noconflict/mode-javascript';
import 'ace-builds/src-noconflict/theme-github';

const CustomTreeView = ({ data }) => {
    console.log("data == "+JSON.stringify(data));
    const [editorContent, setEditorContent] = React.useState(null);

    // Handler for file click
    const onFileSelect = (node) => {
      // Check if the node has content (it's a file, not a folder)
      console.log("node ... "+JSON.stringify(node));
      handleFileClick(node.name)
    }

    const handleFileClick = async (name) => {
        try {
          const response = await fetch('http://localhost:8080/micro/content', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              // Replace with actual payload data
              data: name
            }),
          });
    
          if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
          }
      
          const text = await response.text();
          setEditorContent(text);
        } catch (error) {
          console.error('API call failed:', error);
          // Handle error here
        }
      };

    return (
        <div>
            <div className="directory">
                <TreeView
                    data={data}
                    aria-label="directory tree"
                    nodeRenderer={({
                    element,
                    isBranch,
                    isExpanded,
                    getNodeProps,
                    level,
                    }) => (
                    <div {...getNodeProps()} style={{ paddingLeft: 20 * (level - 1) }}>
                        {isBranch ? (
                        <FolderIcon isOpen={isExpanded} />
                        ) : (
                        <FileIcon filename={element.name} />
                        )}
                        <span onClick={() => onFileSelect(element)}>{element.name}</span>
                    </div>
                    )}
                />
            </div>
            <div style={{ width: '70%', padding: '10px' }}>
                <h3>File Editor</h3>
                {editorContent ? (
                <AceEditor
                    mode="java" // You can dynamically set the mode based on the file type
                    theme="github"
                    name="file-editor"
                    value={editorContent}
                    onChange={(newContent) => {
                    // Handle content changes if needed
                    setEditorContent(editorContent);
                    }}
                    editorProps={{ $blockScrolling: false }}
                    style={{ width: '100%', height: '500px' }}
                />
                ) : (
                <p>Select a file to view its content</p>
                )}
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