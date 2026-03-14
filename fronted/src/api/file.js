import axios from 'axios';

const BASE_URL = 'api/';

export const getFolderList = (parentPath) => {
  return axios.get(`${BASE_URL}folderList?parentPath=${parentPath}`);
};

export const getFileList = (current, pageSize, parentPath) => {
  return axios.get(`${BASE_URL}fileList/${current}/${pageSize}?parentPath=${parentPath}`);
};

export const fileDownload = (parentPath, fileName) => {
  window.location.href = `${BASE_URL}download?parentPath=${parentPath}&fileName=${fileName}`;
};

