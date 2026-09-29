import { Button, Card } from '@heroui/react'

// Placeholder page used to verify the HeroUI + Tailwind setup.
export function DashboardPage() {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <Card>
        <Card.Header>
          <Card.Title>Eben-Ezer Store Management</Card.Title>
          <Card.Description>
            Frontend base configured with React, Vite, HeroUI and Tailwind CSS.
          </Card.Description>
        </Card.Header>
        <Card.Footer>
          <Button variant="primary">Get started</Button>
        </Card.Footer>
      </Card>
    </div>
  )
}
