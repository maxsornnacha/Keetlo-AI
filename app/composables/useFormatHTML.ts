// composables/useFormatHTML.ts
import type { HTMLBeautifyOptions } from 'js-beautify';
import beautify from 'js-beautify'

export function useFormatHTML() {
  const formatHTML = (source: string) =>
    beautify.html(source, {
      indent_size: 2,
      wrap_line_length: 120,
      end_with_newline: true,
      preserve_newlines: true,
      max_preserve_newlines: 2,
      indent_inner_html: true,
    } as HTMLBeautifyOptions)

  return { formatHTML }
}
