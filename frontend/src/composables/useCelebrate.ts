import confetti from 'canvas-confetti'

const COLORS = ['#7c5cff', '#ff5ca8', '#ffb547', '#22d3a0', '#3ba7ff']

export function celebrate(big = false) {
  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduce) return

  if (!big) {
    confetti({ particleCount: 60, spread: 70, origin: { y: 0.7 }, colors: COLORS, scalar: 0.9 })
    return
  }

  const end = Date.now() + 1200
  const frame = () => {
    confetti({ particleCount: 6, angle: 60, spread: 60, origin: { x: 0, y: 0.75 }, colors: COLORS })
    confetti({ particleCount: 6, angle: 120, spread: 60, origin: { x: 1, y: 0.75 }, colors: COLORS })
    if (Date.now() < end) requestAnimationFrame(frame)
  }
  frame()
  setTimeout(() => confetti({ particleCount: 140, spread: 100, origin: { y: 0.6 }, colors: COLORS }), 300)
}
