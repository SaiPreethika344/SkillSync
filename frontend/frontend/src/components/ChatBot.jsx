import { useState, useRef, useEffect } from 'react'
import useWindowWidth from '../hooks/useWindowWidth'
import { sendChatMessage } from '../api'

export default function ChatBot({ userName, topCareer, roadmap }) {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState([
    { role: 'bot', text: `Hi ${userName || 'there'}! 👋 I'm your SkillSync AI career guide. Ask me anything about your roadmap or career path!` }
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const messagesEndRef = useRef(null)
  const width = useWindowWidth()
  const isMobile = width < 768

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = async () => {
    if (!input.trim() || loading) return
    const userMsg = input.trim()
    setInput('')
    setMessages(prev => [...prev, { role: 'user', text: userMsg }])
    setLoading(true)

    const completedCount = roadmap.filter(r => r.isComplete).length
    const systemPrompt = `You are SkillSync AI, a focused career coach. Rules you must ALWAYS follow:
1. NEVER start a response with Hi, Hello, Hey or any greeting after the first message
2. Give direct, specific, actionable answers only
3. When user asks to break down a step, give numbered sub-tasks for ONLY that specific step
4. Maintain conversation context — remember what was discussed earlier in this chat
5. Never repeat the overall roadmap when user asks about a specific step
6. Be concise — maximum 5 lines per response unless user asks for more detail
7. Never ask Are you ready or How does this sound — just give the answer

User: ${userName}. Top career match: ${topCareer}. Roadmap: ${roadmap.length} steps, ${completedCount} completed. Pending steps: ${roadmap.filter(r => !r.isComplete).map(r => r.title).join(', ')}.`

    try {
      const res = await sendChatMessage(userMsg, systemPrompt)
      const reply = res.reply || "I'm here to help! Could you rephrase that?"

      setMessages(prev => [...prev, { role: 'bot', text: reply }])

      // Tired detection — browser notification after 1 hour
      const tiredWords = ['tired', 'exhausted', 'break', 'rest', 'stop', 'enough']
      if (tiredWords.some(w => userMsg.toLowerCase().includes(w))) {
        if ('Notification' in window) {
          Notification.requestPermission().then(permission => {
            if (permission === 'granted') {
              setTimeout(() => {
                new Notification('SkillSync AI — Time to resume! 🚀', {
                  body: `Hey ${userName}, ready to continue your journey to become a ${topCareer}? Your roadmap is waiting!`,
                  icon: '/vite.svg'
                })
              }, 60 * 60 * 1000) // 1 hour
            }
          })
        }
      }
    } catch {
      setMessages(prev => [...prev, { role: 'bot', text: 'Something went wrong. Please try again!' }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      {/* Floating button */}
      <div className="chat-trigger" style={{position:'fixed', bottom:28, right:28, zIndex:1000}}>
        <span
          className="chat-tooltip"
          style={{
            position:'absolute',
            right:64,
            top:'50%',
            transform:'translateY(-50%) translateY(4px)',
            background:'#0f172a',
            color:'white',
            fontSize:12,
            borderRadius:8,
            padding:'8px 10px',
            whiteSpace:'nowrap',
            opacity:0,
            transition:'opacity 0.2s ease, transform 0.2s ease',
            pointerEvents:'none'
          }}
        >
          Ask your AI career guide
        </span>
        <span
          className="chat-pulse-ring"
          style={{
            position:'absolute',
            inset:0,
            borderRadius:'50%',
            background:'rgba(24,95,165,0.35)'
          }}
        />
        <button
          onClick={() => setOpen(prev => !prev)}
          title="Ask your AI career guide"
          style={{
            position:'relative',
            width:52, height:52, borderRadius:'50%',
            background:'#185FA5', border:'none', cursor:'pointer',
            display:'flex', alignItems:'center', justifyContent:'center',
            boxShadow:'0 4px 20px rgba(24,95,165,0.4)',
            transition:'transform 0.2s',
          }}
          onMouseEnter={e => e.currentTarget.style.transform='scale(1.08)'}
          onMouseLeave={e => e.currentTarget.style.transform='scale(1)'}
        >
          {open ? (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          ) : (
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              <circle cx="9" cy="10" r="1" fill="white"/><circle cx="12" cy="10" r="1" fill="white"/><circle cx="15" cy="10" r="1" fill="white"/>
            </svg>
          )}
        </button>
      </div>

      {/* Chat panel */}
      {open && (
        <div style={{
          position:'fixed', bottom: isMobile ? 0 : 92, right: isMobile ? 0 : 28, left: isMobile ? 0 : 'auto', zIndex:999,
          width: isMobile ? '100vw' : 340, height: isMobile ? '70vh' : 480, background:'white',
          borderRadius: isMobile ? '16px 16px 0 0' : 20, boxShadow:'0 8px 40px rgba(0,0,0,0.15)',
          display:'flex', flexDirection:'column', overflow:'hidden',
          border:'1px solid #eee'
        }}>
          {/* Header */}
          <div style={{padding:'16px 20px', background:'#185FA5', display:'flex', alignItems:'center', gap:10}}>
            <div style={{width:32, height:32, borderRadius:'50%', background:'rgba(255,255,255,0.2)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
            </div>
            <div>
              <p style={{color:'white', fontSize:14, fontWeight:600, margin:0}}>SkillSync AI Guide</p>
              <p style={{color:'rgba(255,255,255,0.7)', fontSize:11, margin:0}}>Your personal career assistant</p>
            </div>
          </div>

          {/* Messages */}
          <div style={{flex:1, overflowY:'auto', padding:'16px', display:'flex', flexDirection:'column', gap:10}}>
            {messages.map((m, i) => (
              <div key={i} style={{display:'flex', justifyContent: m.role === 'user' ? 'flex-end' : 'flex-start'}}>
                <div style={{
                  maxWidth:'80%', padding:'10px 14px', borderRadius: m.role === 'user' ? '16px 16px 4px 16px' : '16px 16px 16px 4px',
                  background: m.role === 'user' ? '#185FA5' : '#f5f5f5',
                  color: m.role === 'user' ? 'white' : '#111',
                  fontSize:13, lineHeight:1.5
                }}>
                  {m.text}
                </div>
              </div>
            ))}
            {loading && (
              <div style={{display:'flex', justifyContent:'flex-start'}}>
                <div style={{background:'#f5f5f5', borderRadius:'16px 16px 16px 4px', padding:'10px 16px'}}>
                  <span style={{fontSize:18, letterSpacing:2}}>···</span>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <div style={{padding:'12px 16px', borderTop:'1px solid #f0f0f0', display:'flex', gap:8}}>
            <input
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSend()}
              placeholder="Ask your career guide..."
              style={{flex:1, border:'1px solid #eee', borderRadius:12, padding:isMobile ? '14px 16px' : '10px 14px', fontSize:13, minHeight:isMobile ? 48 : 'auto', outline:'none'}}
            />
            <button
              onClick={handleSend}
              disabled={loading || !input.trim()}
              style={{background:'#185FA5', color:'white', border:'none', borderRadius:12, padding:isMobile ? '14px 16px' : '10px 14px', cursor:'pointer', opacity: loading || !input.trim() ? 0.5 : 1}}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
              </svg>
            </button>
          </div>
        </div>
      )}
    </>
  )
}