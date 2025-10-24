import { useState } from "react"

import {
  Box,
  Button,
  Container,
  TextField,
  Typography,
  Paper,
  Divider,
  IconButton,
  InputAdornment,
  Alert,
} from "@mui/material"
import { Visibility, VisibilityOff } from "@mui/icons-material"
import GitHubIcon from "@mui/icons-material/GitHub"
import GoogleIcon from "@mui/icons-material/Google"


export const Register = () => {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [error, setError] = useState("")

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")

    if (password !== confirmPassword) {
      setError("Le password non corrispondono")
      return
    }

    // Controllo password validation
    if (password.length < 8) {
      setError("La password deve essere di almeno 8 caratteri")
      return
    }

    // Logica di registrazione
    console.log("Registrazione con:", { email, password })
  }

  const handleGoogleSignIn = () => {
    // Logica per accesso con Google
    console.log("Accesso con Google")
  }

  const handleGitHubSignIn = () => {
    // Logica per accesso con GitHub
    console.log("Accesso con GitHub")
  }

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          minHeight: "100vh",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          py: 4,
        }}
      >
        <Paper
          elevation={3}
          sx={{
            p: 4,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            width: "100%",
            borderRadius: 2,
          }}
        >
          <Typography component="h1" variant="h4" fontWeight="bold" gutterBottom>
            Registrati
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Crea un nuovo account per iniziare
          </Typography>

          {error && (
            <Alert severity="error" sx={{ width: "100%", mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ width: "100%" }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Indirizzo Email"
              name="email"
              autoComplete="email"
              autoFocus
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              type="email"
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type={showPassword ? "text" : "password"}
              id="password"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
                      onClick={() => setShowPassword(!showPassword)}
                      edge="end"
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="confirmPassword"
              label="Conferma Password"
              type={showConfirmPassword ? "text" : "password"}
              id="confirmPassword"
              autoComplete="new-password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle confirm password visibility"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      edge="end"
                    >
                      {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2, py: 1.5 }}>
              Registrati
            </Button>

            <Box sx={{ textAlign: "center", mb: 2 }}>
             
                <Typography variant="body2" color="primary" sx={{ "&:hover": { textDecoration: "underline" } }}>
                  Password dimenticata?
                </Typography>
              
            </Box>

            <Divider sx={{ my: 2 }}>
              <Typography variant="body2" color="text.secondary">
                OPPURE
              </Typography>
            </Divider>

            <Button
              fullWidth
              variant="outlined"
              startIcon={<GoogleIcon />}
              onClick={handleGoogleSignIn}
              sx={{ mb: 1.5, py: 1.5 }}
            >
              Continua con Google
            </Button>

            <Button
              fullWidth
              variant="outlined"
              startIcon={<GitHubIcon />}
              onClick={handleGitHubSignIn}
              sx={{ mb: 2, py: 1.5 }}
            >
              Continua con GitHub
            </Button>

            <Box sx={{ textAlign: "center", mt: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Hai già un account?{" "}
                <a href="/login" style={{ textDecoration: "none" }}>
                  <Typography
                    component="span"
                    variant="body2"
                    color="primary"
                    sx={{ fontWeight: "bold", "&:hover": { textDecoration: "underline" } }}
                  >
                    Accedi
                  </Typography>
                </a>
              </Typography>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  )
}