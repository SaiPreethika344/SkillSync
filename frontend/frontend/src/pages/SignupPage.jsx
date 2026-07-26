import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import useWindowWidth from '../hooks/useWindowWidth'
import Navbar from '../components/Navbar'
import { registerUser } from '../api'

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
