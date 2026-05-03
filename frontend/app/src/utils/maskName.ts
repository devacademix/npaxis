export const maskName = (name: string) => {
  if (!name) return '';

  return name
    .trim()
    .split(/\s+/)
    .map((word) => {
      if (!word.length) return '';
      return `${word[0].toUpperCase()}XXXX`;
    })
    .join(' ');
};

export default maskName;
