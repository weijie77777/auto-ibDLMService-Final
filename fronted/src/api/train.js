import request from '@/utils/request';

export function train(data) {
  return request({
    url: '/train',
    method: 'post',
    data: data
  });
}

export function save() {
  return request({
    url: '/savemodel',
    method: 'get'
  });
}

