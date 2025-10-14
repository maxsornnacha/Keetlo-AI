<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import Prism from 'prismjs';
import 'prismjs/themes/prism-tomorrow.css'; // VSCode-like dark theme
import 'prismjs/components/prism-markup'; // HTML syntax (Prism calls HTML "markup")

const props = defineProps<{ html: string }>();
const codeRef = ref<HTMLElement | null>(null);

// Function to highlight HTML safely
const highlight = (html: string) => {
  if (codeRef.value && Prism.languages.markup) {
    codeRef.value.innerHTML = Prism.highlight(html, Prism.languages.markup, 'html');
  }
};

// Initial highlight on mount
onMounted(() => {
  highlight(props.html);
});

// Update highlight whenever html prop changes
watch(() => props.html, (newHtml) => {
  highlight(newHtml);
});
</script>

<template>
  <pre class="rounded-md overflow-auto shadow-lg p-4 bg-[#1e1e1e]">
    <code ref="codeRef" class="language-html"/>
  </pre>
</template>

<style scoped>
pre {
  font-family: 'Fira Code', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  color: #d4d4d4;
}

/* Optional: add scrollbars for long code */
pre::-webkit-scrollbar {
  height: 8px;
  width: 8px;
}
pre::-webkit-scrollbar-thumb {
  background: #555;
  border-radius: 4px;
}
pre::-webkit-scrollbar-track {
  background: #1e1e1e;
}
</style>
