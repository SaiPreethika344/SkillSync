import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import useWindowWidth from '../hooks/useWindowWidth'
import Navbar from '../components/Navbar'
import { BACKEND_BASE_URL, registerUser } from '../api'

export default function SignupPage() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const navigate = useNavigate()
  const width = useWindowWidth()
  const isMobile = width < 768

  const handleSignup = async (e) => {
    e.preventDefault()
    const res = await registerUser(name, email, password)
    if (res.token) {
      localStorage.setItem('token', res.token)
      navigate('/dashboard')
    } else {
      alert(res.message || 'Registration failed')
    }
  }

  return (
    <div style={{minHeight:'100vh',background:'#f9f9f9'}}>
      <Navbar />
      <div style={{maxWidth:440,width:'100%',margin:'0 auto',padding:isMobile ? '80px 16px 60px' : '100px 24px 80px'}}>
        <div style={{background:'white',borderRadius:20,border:'1px solid #eee',padding:isMobile ? 24 : 40}}>
          <h1 style={{fontSize:isMobile ? 22 : 26,fontWeight:700,color:'#111',marginBottom:4,textAlign:isMobile ? 'center' : 'left'}}>Create your account</h1>
          <p style={{color:'#666',fontSize:14,marginBottom:isMobile ? 24 : 32,textAlign:isMobile ? 'center' : 'left'}}>Free forever. No credit card needed.</p>
          <button onClick={() => window.location.href = `${BACKEND_BASE_URL}/oauth2/authorization/google`} style={{width:'100%',border:'1px solid #eee',borderRadius:12,padding:'12px',display:'flex',alignItems:'center',justifyContent:'center',gap:10,fontSize:14,fontWeight:500,color:'#333',background:'white',cursor:'pointer',marginBottom:24}}>
            <svg width="18" height="18" viewBox="0 0 48 48"><path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.08 17.74 9.5 24 9.5z"/><path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/><path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/><path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.18 1.48-4.97 2.31-8.16 2.31-6.26 0-11.57-3.59-13.46-8.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/></svg>
            Continue with Google
          </button>
          <div style={{display:'flex',alignItems:'center',gap:12,marginBottom:24}}>
            <div style={{flex:1,height:1,background:'#f0f0f0'}}></div>
            <span style={{fontSize:12,color:'#999'}}>or</span>
            <div style={{flex:1,height:1,background:'#f0f0f0'}}></div>
          </div>
          <div style={{display:'flex',flexDirection:'column',gap:16}}>
            <div>
              <label style={{display:'block',fontSize:13,fontWeight:500,color:'#333',marginBottom:6}}>Full name</label>
              <input type="text" value={name} onChange={e => setName(e.target.value)} placeholder="Saran" style={{width:'100%',height:isMobile ? 48 : 'auto',border:'1px solid #eee',borderRadius:10,padding:'12px 16px',fontSize:14,outline:'none'}} />
            </div>
            <div>
              <label style={{display:'block',fontSize:13,fontWeight:500,color:'#333',marginBottom:6}}>Email</label>
              <input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="you@example.com" style={{width:'100%',height:isMobile ? 48 : 'auto',border:'1px solid #eee',borderRadius:10,padding:'12px 16px',fontSize:14,outline:'none'}} />
            </div>
            <div>
              <label style={{display:'block',fontSize:13,fontWeight:500,color:'#333',marginBottom:6}}>Password</label>
              <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" style={{width:'100%',height:isMobile ? 48 : 'auto',border:'1px solid #eee',borderRadius:10,padding:'12px 16px',fontSize:14,outline:'none'}} />
            </div>
            <button onClick={handleSignup} style={{width:'100%',background:'#185FA5',color:'white',border:'none',padding:'14px',borderRadius:12,fontSize:14,fontWeight:600,cursor:'pointer'}}>Create account</button>
          </div>
          <p style={{textAlign:'center',fontSize:13,color:'#666',marginTop:24}}>Already have an account? <Link to="/login" style={{color:'#185FA5',fontWeight:500,textDecoration:'none'}}>Log in</Link></p>
        </div>
      </div>
    </div>
  )
}
