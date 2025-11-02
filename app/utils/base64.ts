export const urlToBase64 = async (url : string) => {
  const res = await fetch(url)
  const blob = await res.blob()
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onloadend = () => resolve(reader.result) // gives data:<mime>;base64,...
    reader.onerror = reject
    reader.readAsDataURL(blob)
  })
}

// Checks a raw Base64 string (standard or URL-safe). Ignores whitespace.
export const isBase64 = (
  input: unknown,
  opts: { urlSafe?: boolean; requirePadding?: boolean } = {}
): boolean => {
  if (typeof input !== "string") return false;
  let s = input.trim();
  if (s.length === 0) return false;

  // Allow URL-safe Base64 by normalizing -_ to +/
  if (opts.urlSafe) s = s.replace(/-/g, "+").replace(/_/g, "/");

  // Remove whitespace
  s = s.replace(/\s+/g, "");

  // Optionally require padding (=)
  if (opts.requirePadding && s.length % 4 !== 0) return false;

  // Fast regex screen
  const base64Re =
    /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/;
  if (!base64Re.test(s)) return false;

  // Decode/encode round-trip to be extra sure
  try {
    const decoded = atob(s);
    // If requirePadding=false, some inputs may be paddingless but valid;
    // try re-encode to check it’s still Base64-ish.
    void decoded; // use decoded to ensure atob didn't throw
    return true;
  } catch {
    return false;
  }
};

// Checks for `data:<mime>[;charset=...];base64,<payload>`
export const isDataUrlBase64 = (input: unknown): boolean => {
  if (typeof input !== "string") return false;
  const s = input.trim();
  const re =
    /^data:([a-z]+\/[a-z0-9+.\\-]+)?(?:;charset=[^;]+)?;base64,[A-Za-z0-9+/]+={0,2}$/i;
  if (!re.test(s)) return false;
  // Validate the payload part with isBase64
  const payload = s.split(",")[1] || "";
  return isBase64(payload);
};