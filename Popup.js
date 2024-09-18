import CustomTreeView from "./CustomTreeView";
import { useEffect } from "react";

export const Popup = ({ query, setter, data }) => {
  
  // Trigger Fullscreen mode on mount
  useEffect(() => {
    if (document.documentElement.requestFullscreen) {
      document.documentElement.requestFullscreen();
    }
  }, []);

  // Exit Fullscreen mode when the popup closes
  const handleClose = () => {
    
     if (document.exitFullscreen && document.fullscreenElement) {
       document.exitFullscreen();
     }
    setter(false);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-gray-900 opacity-50"></div>
      <div className="relative p-5 border w-full h-full max-custom-w-none shadow-lg rounded-md bg-white">
        
        {/* Cross mark in the top-right corner */}
        <button 
          className="absolute top-4 right-4 text-gray-500 hover:text-gray-800 text-2xl"
          onClick={handleClose}
        >
          &times;
        </button>

        <h3 className="text-xl font-semibold text-indigo-800 tracking-wide">
          Project Creation Complete! Explore the Generated Folder Structure Below. 
          You want to open this in IntelliJ! 
          <a 
            href="http://your-intellij-link-here" 
            className="text-blue-600 hover:underline" 
            target="_blank" 
            rel="noopener noreferrer"
          >
            Click
          </a>
        </h3>
        <div style={{ height: '50px' }}></div>
        <CustomTreeView data={data}></CustomTreeView>
      </div>
    </div>
  );
};
