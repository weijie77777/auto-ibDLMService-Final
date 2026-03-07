export function getToken() {
  return localStorage.getItem('userInfo');
}

export function setToken(userInfo) {
  return localStorage.setItem('userInfo', JSON.stringify(userInfo));
}

export function removeToken() {
  return localStorage.removeItem('userInfo');
}

