import type { SVGProps } from 'react'

// @gravity-ui/icons has no truck; simple outline truck in the same line style as the other icons.
export function TruckIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
      {...props}
    >
      <path d="M2.5 6.5h11v10h-11z" />
      <path d="M13.5 10h4l3 3v3.5h-7" />
      <circle cx="6.5" cy="17.5" r="1.75" fill="currentColor" stroke="none" />
      <circle cx="17" cy="17.5" r="1.75" fill="currentColor" stroke="none" />
    </svg>
  )
}
