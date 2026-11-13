/** Russian plural forms: "no | one | few | many". */
export function russianPlural(choice: number, choicesLength: number): number {
  if (choice === 0) return 0
  if (choicesLength < 4) return choice === 1 ? 1 : 2
  const mod10 = choice % 10
  const mod100 = choice % 100
  if (mod10 === 1 && mod100 !== 11) return 1
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return 2
  return 3
}
