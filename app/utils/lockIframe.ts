export function lockIframe(e: Event) {
  const frame = e.target as HTMLIFrameElement
  const doc = frame.contentDocument
  const win = frame.contentWindow
  if (!doc || !win) return

  // 1) Force in-frame navigation
  const bases = doc.querySelectorAll('base')
  if (bases.length === 0) {
    const base = doc.createElement('base')
    base.setAttribute('target', '_self')
    doc.head?.prepend(base)
  } else {
    bases.forEach(b => b.setAttribute('target', '_self'))
  }
  doc.querySelectorAll('a').forEach(a => {
    if (!a.hasAttribute('target')) a.setAttribute('target', '_self')
  })

  // 2) Prevent "#" from jumping, and optionally block all nav during preview
  doc.addEventListener('click', (ev) => {
    const a = (ev.target as HTMLElement).closest('a') as HTMLAnchorElement | null
    if (!a) return
    // Always prevent default to avoid escapes / jumps
    ev.preventDefault()

    // If you want to **allow** in-iframe nav for real URLs, uncomment:
    // if (href && href !== '#') { try { doc.location.href = href } catch {} }
  }, { capture: true })

  // 3) Kill meta-refresh redirects
  doc.querySelectorAll('meta[http-equiv="refresh"]').forEach(m => m.remove())
}