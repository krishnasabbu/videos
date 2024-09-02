import FolderTreeView from "./FolderTreeView";

export const Popup = ({ query, setter, data }) => (
  <div className="fixed inset-0 z-50 flex items-center justify-center">
  <div className="absolute inset-0 bg-gray-900 opacity-50"></div>
  <div className="relative p-5 border w-1/2 shadow-lg rounded-md bg-white">
    <h3 className="text-lg leading-6 font-medium text-gray-900">
        Project is Successfully Created. Below is the folder structure.
    </h3>
    <div style={{ height: '20px'}}></div>
    <FolderTreeView data={data} />
    <div className="mt-3 text-center">
      <div className="items-center px-4 py-3">
        <button
          id="ok-btn"
          className="px-4 py-2 bg-green-500 text-white text-base font-medium rounded-md w-full shadow-sm hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-300"
          onClick={() => setter(false)}
        >
          OK
        </button>
      </div>
    </div>
  </div>
</div>
);
