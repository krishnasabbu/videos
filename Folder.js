import React, { useState, useEffect } from "react";
import { useNode } from "@craftjs/core";

export const FolderInput = ({ folderName, userEditable = true }) => {
  const {
    connectors: { connect, drag },
    selected,
    actions: { setProp },
  } = useNode((state) => ({
    selected: state.events.selected,
    dragged: state.events.dragged,
  }));

  const [editable, setEditable] = useState(false);

  useEffect(() => {
    if (!selected) {
      if (userEditable) {
        setEditable(false);
      }
    }
  }, [selected, userEditable]);

  return (
    <div
      ref={(ref) => connect(drag(ref))}
      onClick={() => selected && userEditable && setEditable(true)}
    >
      {userEditable ? (
        <input
          type="file"
          webkitdirectory="true"
          directory="true"
          onChange={(e) =>
            setProp(
              (props) => (props.folderName = e.target.files[0]?.webkitRelativePath || ""),
              500
            )
          }
          style={{ display: "block", margin: "10px 0" }}
        />
      ) : (
        <p>{folderName}</p>
      )}
    </div>
  );
};

const FolderInputSettings = () => {
  const {
    actions: { setProp },
    folderName,
  } = useNode((node) => ({
    folderName: node.data.props.folderName,
  }));

  return (
    <label>
      Folder Name
      <input
        type="text"
        value={folderName || ""}
        onChange={(e) => {
          setProp((props) => (props.folderName = e.target.value), 1000);
        }}
      />
    </label>
  );
};

export const FolderInputDefaultProps = {
  folderName: "My Folder",
};

FolderInput.craft = {
  props: FolderInputDefaultProps,
  related: {
    settings: FolderInputSettings,
  },
};
