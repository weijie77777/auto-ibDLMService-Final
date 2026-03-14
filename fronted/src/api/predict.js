import request from '@/utils/request';

export function predict() {
  return request({
    url: '/test'
  });
}

export function TrainModelPredict() {
  return request({
    url: '/usemodel'
  });
}

