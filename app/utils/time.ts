const now = new Date();

const pad = (n : number, size = 2) => n.toString().padStart(size, '0');

export const timeStamp =
  pad(now.getDate()) +
  pad(now.getMonth() + 1) +
  now.getFullYear() +
  pad(now.getHours()) +
  pad(now.getMinutes()) +
  pad(now.getSeconds()) +
  pad(now.getMilliseconds(), 3);