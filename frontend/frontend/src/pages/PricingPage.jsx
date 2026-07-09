import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { upgradeUser } from '../api'

const freeFeatures = [
  '3 career analyses',
  'Basic skill gap',
  '3 career matches',
  'Learning roadmap'
]

const proFeatures = [
  'Unlimited analyses',
  'All 5 career matches',
  'Full skill gap',
  'Live job matches',
  'AI career guide',
  'Priority support'
]

export default function PricingPage() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)

  const handleUpgrade = async () => {
    const token = localStorage.getItem('token')
    if (!token) {
      navigate('/login')
      return
    }

    try {
      setLoading(true)
      const result = await upgradeUser()
      if (result?.success === false) {
        alert(result.message || 'Upgrade failed. Please try again.')
        return
      }

      localStorage.setItem('isPremium', 'true')
      navigate('/dashboard', { state: { successMessage: 'Upgrade successful! Pro features unlocked.' } })
    } catch {
      alert('Upgrade failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh' }}>
      <Navbar />
      <div style={{ maxWidth: 960, margin: '0 auto', padding: '110px 24px 80px' }}>
        <div style={{ textAlign: 'center', marginBottom: 44 }}>
          <h1 style={{ fontSize: 36, fontWeight: 700, color: '#111', marginBottom: 10 }}>Simple pricing</h1>
          <p style={{ color: '#666', fontSize: 16 }}>Choose your plan and grow with SkillSync AI.</p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 18 }}>
          <div className="premium-card" style={{ background: 'white', border: '1px solid #e9edf2', borderRadius: 18, padding: 24 }}>
            <p style={{ color: '#64748b', fontSize: 13, fontWeight: 600, marginBottom: 8 }}>Free</p>
            <h2 style={{ fontSize: 30, color: '#111', marginBottom: 4 }}>₹0</h2>
            <p style={{ color: '#666', marginBottom: 18 }}>forever</p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {freeFeatures.map((feature) => (
                <p key={feature} style={{ color: '#334155', fontSize: 14 }}>- {feature}</p>
              ))}
            </div>
          </div>

          <div className="premium-card" style={{ background: 'white', border: '2px solid #185FA5', borderRadius: 18, padding: 24, boxShadow: '0 10px 24px rgba(24,95,165,0.08)' }}>
            <p style={{ color: '#185FA5', fontSize: 13, fontWeight: 700, marginBottom: 8 }}>Pro</p>
            <h2 style={{ fontSize: 30, color: '#111', marginBottom: 4 }}>₹399</h2>
            <p style={{ color: '#666', marginBottom: 18 }}>lifetime</p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 20 }}>
              {proFeatures.map((feature) => (
                <p key={feature} style={{ color: '#0f172a', fontSize: 14 }}>- {feature}</p>
              ))}
            </div>
            <div style={{ background: '#f0f0f0', color: '#999', padding: '12px 24px', borderRadius: 12, fontSize: 14, fontWeight: 500, display: 'block', textAlign: 'center' }}>
              Coming Soon
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
