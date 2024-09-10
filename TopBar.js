import { useEditor } from "@craftjs/core";
import React, { useEffect } from 'react';
import {
  ArrowCircleLeftIcon,
  ArrowCircleRightIcon,
  ArrowUpIcon,
  SaveIcon,
  CheckIcon,
  PencilIcon,
} from "@heroicons/react/outline";
import { useState } from "react";
import { Panel } from "./Panel";
import { Popup } from "./Popup";
import CustomTreeView from "./CustomTreeView";
import { flattenTree } from "react-accessible-treeview";

const EditButton = ({}) => {
  const { actions, query, enabled } = useEditor((state) => ({
    enabled: state.options.enabled,
  }));

  
  return (
    <button
      className={`text-white ${
        enabled
          ? "bg-green-700 hover:bg-green-800"
          : "bg-blue-700 hover:bg-blue-800"
      } focus:ring-4 focus:ring-blue-300 font-medium rounded-md text-xs px-2 py-1 mr-2 mb-2`}
      onClick={() =>
        actions.setOptions((options) => (options.enabled = !enabled))
      }
    >
      {enabled ? (
        <>
          <CheckIcon className="w-5 h-5 inline-block mr-2" />
          Finish
        </>
      ) : (
        <>
          <PencilIcon className="w-5 h-5 inline-block mr-2" />
          Edit
        </>
      )}
    </button>
  );
};

const TopBarButton = ({ children, className, ...props }) => {
  return (
    <button
      className={`focus:outline-none text-white focus:ring-2 focus:ring-green-300 font-medium rounded-lg text-xs px-2.5 py-1 mr-2 mb-2 ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};


export const TopBar = () => {

  
  const [showPopup, setShowPopup] = useState(false);
  const [folderData, setFolderData] = useState([]);

  const { actions, canUndo, canRedo, query } = useEditor((_, query) => ({
    canUndo: query.history.canUndo(),
    canRedo: query.history.canRedo(),
  }));

  const handleGenerateMicroService = async () => {
    try {
      const response = await fetch('http://localhost:8080/micro', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          // Replace with actual payload data
          data: query.serialize()
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
  
      const data = await response.json();
      console.log(data);
      const customData = flattenTree(data);
      console.log(customData);
      setFolderData(customData);
      setShowPopup(true);
    } catch (error) {
      console.error('API call failed:', error);
      // Handle error here
    }
  };


  return (
    <Panel className="mb-2 pt-2 pb-0 flex">
      <div className="w-1/2 fill">
        <TopBarButton
          disabled={!canUndo}
          onClick={() => actions.history.undo()}
          className={`bg-gray-200 ${
            canUndo ? "text-gray-900" : "text-gray-400"
          }`}
        >
          <ArrowCircleLeftIcon className="w-5 h-5 inline-block mr-2" />
          Undo
        </TopBarButton>
        <TopBarButton
          disabled={!canRedo}
          onClick={() => actions.history.redo()}
          className={`bg-gray-200 ${
            canRedo ? "text-gray-900" : "text-gray-400"
          }`}
        >
          <ArrowCircleRightIcon className="w-5 h-5 inline-block mr-2" />
          Redo
        </TopBarButton>
        <TopBarButton
          onClick={() => {
            handleGenerateMicroService();
          }}
          className="text-white bg-sky-700 hover:bg-sky-800"
        >
          <SaveIcon className="w-4 h-4 inline-block mr-2" />
          Generate MicroService
        </TopBarButton>
      </div>
      <div className="w-1/2 fill text-right">
        <EditButton />
      </div>

      {showPopup && <Popup setter={setShowPopup} query={query} data={folderData} />}
    </Panel>
  );
};


