import { useState, type FormEvent } from 'react'
import { Alert, Button, Checkbox, FieldError, Form, InputGroup, Label, TextField } from '@heroui/react'
import Eye from '@gravity-ui/icons/Eye'
import EyeSlash from '@gravity-ui/icons/EyeSlash'
import Lock from '@gravity-ui/icons/Lock'
import Person from '@gravity-ui/icons/Person'
import { useLogin } from '../hooks/useLogin'

export function LoginForm() {
  const { submit, isPending, error, errorKey } = useLogin()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(true)
  const [showPassword, setShowPassword] = useState(false)

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    void submit({ username, password }, remember)
  }

  return (
    <Form onSubmit={handleSubmit} className="flex flex-col gap-5">
      {error && (
        <Alert key={errorKey} status="danger" role="alert" className="animate-shake motion-reduce:animate-none">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Title>{error}</Alert.Title>
          </Alert.Content>
        </Alert>
      )}

      <TextField
        name="username"
        value={username}
        onChange={setUsername}
        isRequired
        autoComplete="username"
        autoFocus
      >
        <Label>Usuario</Label>
        <InputGroup className="h-12">
          <InputGroup.Prefix>
            <Person className="size-4 text-muted" />
          </InputGroup.Prefix>
          <InputGroup.Input placeholder="tu.usuario" />
        </InputGroup>
        <FieldError>Ingresa tu usuario.</FieldError>
      </TextField>

      <TextField
        name="password"
        type={showPassword ? 'text' : 'password'}
        value={password}
        onChange={setPassword}
        isRequired
        autoComplete="current-password"
      >
        <Label>Contraseña</Label>
        <InputGroup className="h-12">
          <InputGroup.Prefix>
            <Lock className="size-4 text-muted" />
          </InputGroup.Prefix>
          <InputGroup.Input placeholder="••••••••" />
          <InputGroup.Suffix>
            <Button
              isIconOnly
              size="sm"
              variant="ghost"
              aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              onPress={() => setShowPassword((visible) => !visible)}
            >
              {showPassword ? <EyeSlash className="size-4" /> : <Eye className="size-4" />}
            </Button>
          </InputGroup.Suffix>
        </InputGroup>
        <FieldError>Ingresa tu contraseña.</FieldError>
      </TextField>

      <Checkbox isSelected={remember} onChange={setRemember}>
        <Checkbox.Content>
          <Checkbox.Control>
            <Checkbox.Indicator />
          </Checkbox.Control>
          <Label>Recordar sesión</Label>
        </Checkbox.Content>
      </Checkbox>

      <Button type="submit" variant="primary" size="lg" fullWidth isPending={isPending}>
        {isPending ? 'Ingresando…' : 'Iniciar sesión'}
      </Button>
    </Form>
  )
}
