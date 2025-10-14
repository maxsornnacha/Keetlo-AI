// modules/pageStream.ts
export interface WebPage {
  generatedPageId: string;
  projectMessageId: string;
  path: string;
  label: string;
  htmlContent: string;
  width: number;
  height: number;
  top:number;
  left:number;
}

export async function streamPages(
  input: string,
  apiBase: string,
  token: string,
  onChunk?: (chunk: string) => void,
  onPage?: (page: WebPage) => void
) {
  const res = await fetch(`${apiBase}/page/generate`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "text/event-stream",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ input }),
  });

  const reader = res.body?.getReader();
  if (!reader) return;

  const decoder = new TextDecoder();
  let buffer = "";
  let jsonBuffer = "";
  let braceCount = 0;

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split("\n");
    buffer = lines.pop() || "";

    for (const line of lines) {
      if (!line.startsWith("data:")) continue;
      const cleaned = line.replace(/^data:\s*/, "").trim();
      if (!cleaned || cleaned === "[DONE]") continue;

      // Try parse as full JSON first (your WebPage object)
      let handled = false;
      try {
        const obj: WebPage = JSON.parse(cleaned);
        // Clean up content formatting
        obj.htmlContent = obj.htmlContent
          .replace(/\\n/g, "")
          .replace(/\\r/g, "")
          .replace(/\\t/g, " ")
          .replace(/```/g, "")
          .trim();
        onPage?.(obj);
        handled = true;
        continue; // done, skip delta logic
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      } catch (error){
        // console.log(error)
      }

      if (handled) continue;

      // If not a full object, try OpenAI delta style
      let delta: string | undefined;
      try {
        const json = JSON.parse(cleaned);
        if (json.choices && json.choices[0].delta?.content) {
          delta = json.choices[0].delta.content;
        }
      } catch {
        delta = cleaned; // fallback raw text
      }

      if (delta) {
        onChunk?.(delta);

        // Accumulate JSON if braces exist
        const safeDelta = delta.replace(/```json/g, "").replace(/```/g, "");
        for (const char of safeDelta) {
          if (char === "{") {
            if (braceCount === 0) jsonBuffer = "";
            braceCount++;
          }

          if (braceCount > 0) {
            jsonBuffer += char;
          }

          if (char === "}") {
            braceCount--;
            if (braceCount === 0) {
              try {
                const obj: WebPage = JSON.parse(jsonBuffer);
                obj.htmlContent = obj.htmlContent
                  .replace(/\\n/g, "")
                  .replace(/\\r/g, "")
                  .replace(/\\t/g, " ")
                  .replace(/```/g, "")
                  .trim();
                onPage?.(obj);
              } catch (err) {
                console.error("Failed to parse JSON block:", err, jsonBuffer);
              }
              jsonBuffer = "";
            }
          }
        }
      }
    }
  }
}
