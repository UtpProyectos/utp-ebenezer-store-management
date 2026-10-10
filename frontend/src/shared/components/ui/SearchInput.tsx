import { InputGroup, TextField } from '@heroui/react'
import Magnifier from '@gravity-ui/icons/Magnifier'

interface SearchInputProps {
  value: string
  onChange: (value: string) => void
  /** Accessible name; also used as placeholder when none is given. */
  label: string
  placeholder?: string
  isInvalid?: boolean
  autoFocus?: boolean
  /** Layout only (width / flex). The look is the same on every screen. */
  className?: string
}

// Single search field for every list (pill, magnifier, field border). Do not restyle it per screen.
export function SearchInput({ value, onChange, label, placeholder, isInvalid, autoFocus, className }: SearchInputProps) {
  return (
    <TextField
      aria-label={label}
      value={value}
      onChange={onChange}
      isInvalid={isInvalid}
      autoFocus={autoFocus}
      className={className}
    >
      <InputGroup className="h-13 w-full rounded-full">
        <InputGroup.Prefix className="border-e-0 pr-3 pl-5">
          <Magnifier aria-hidden="true" className="size-5 text-muted" />
        </InputGroup.Prefix>
        <InputGroup.Input type="search" placeholder={placeholder ?? label} className="pe-5 text-base" />
      </InputGroup>
    </TextField>
  )
}
