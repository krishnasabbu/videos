import React, { useState } from "react";
import { useNode } from "@craftjs/core";

export const FolderInput = ({ folderPath, userEditable = true }) => {
  const {
    connectors: { connect, drag },
    selected,
    actions: { setProp },
  } = useNode((state) => ({
    selected: state.events.selected,
    dragged: state.events.dragged,
  }));

  const [editable, setEditable] = useState(false);

  const handleFolderSelect = (e) => {
    const files = e.target.files;
    if (files.length > 0) {
      const path = files[0].webkitRelativePath.split("/")[0];
      setProp((props) => (props.folderPath = path), 500);
    }
  };

  return (
    <div
      ref={(ref) => connect(drag(ref))}
      onClick={() => selected && userEditable && setEditable(true)}
    >
      {userEditable ? (
        <>
          <label
            htmlFor="folderInput"
            style={{
              display: "inline-block",
              padding: "6px 12px",
              cursor: "pointer",
              backgroundColor: "#007bff",
              color: "white",
              borderRadius: "4px",
              textAlign: "center",
            }}
          >
            Select Folder
          </label>
          <input
            type="file"
            id="folderInput"
            style={{ display: "none" }}
            webkitdirectory="true"
            directory="true"
            onChange={handleFolderSelect}
          />
        </>
      ) : (
        <p>{folderPath}</p>
      )}
    </div>
  );
};

const FolderInputSettings = () => {
  const {
    actions: { setProp },
    folderPath,
  } = useNode((node) => ({
    folderPath: node.data.props.folderPath,
  }));

  return (
    <label>
      Folder Path
      <input
        type="text"
        value={folderPath || ""}
        onChange={(e) => {
          setProp((props) => (props.folderPath = e.target.value), 1000);
        }}
      />
    </label>
  );
};

export const FolderInputDefaultProps = {
  folderPath: "",
};

FolderInput.craft = {
  props: FolderInputDefaultProps,
  related: {
    settings: FolderInputSettings,
  },
};
