import React, { useState } from "react";
import TreeView from "react-treeview";
import "react-treeview/react-treeview.css";

const FolderTreeView = ({ data }) => {
  const [expandedNodes, setExpandedNodes] = useState({});

  const toggleNode = (nodeName) => {
    setExpandedNodes((prevState) => ({
      ...prevState,
      [nodeName]: !prevState[nodeName],
    }));
  };

  const renderTreeNodes = (nodes, parentPath = "") => {
    return nodes.map((node) => {
      const nodePath = parentPath ? `${parentPath}/${node.name}` : node.name;
      const hasChildren = node.children && node.children.length > 0;
      const isExpanded = expandedNodes[nodePath] || false;

      return (
        <TreeView
          key={nodePath}
          nodeLabel={node.name}
          defaultCollapsed={!isExpanded}
          onClick={() => toggleNode(nodePath)}
        >
          {hasChildren && isExpanded && renderTreeNodes(node.children, nodePath)}
        </TreeView>
      );
    });
  };

  return <div>{renderTreeNodes(data)}</div>;
};

export default FolderTreeView;
