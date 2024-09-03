import React from "react";
import { DiCss3, DiJavascript, DiNpm, DiJava } from "react-icons/di";
import { FaList, FaRegFolder, FaRegFolderOpen, FaFileCode, FaFileAlt } from "react-icons/fa";
import TreeView from "react-accessible-treeview";

const CustomTreeView = ({ data }) => (
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

            {element.name}
          </div>
        )}
      />
    </div>
  </div>
);

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
