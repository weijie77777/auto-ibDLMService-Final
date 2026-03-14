import request from '@/utils/request';

export function addEmployee(data) {
  return request({
    url: '/user/sign',
    method: 'post',
    data: data
  });
}

