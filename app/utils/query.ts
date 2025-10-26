export const setQueryParam = (
  key: string,
  value?: string | number | null,
  push = false
) => {
  const router = useRouter();
  const route = useRoute();
  const next = { ...route.query };
  // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
  if (value === null || value === undefined || value === "") delete next[key];
  else next[key] = String(value);
  return push ? router.push({ query: next }) : router.replace({ query: next });
};
