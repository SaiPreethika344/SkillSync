export default function DashboardPreview() {
  return (
    <section style={{padding:'80px 24px', background:'#f9f9f9'}}>
      <div style={{maxWidth:800, margin:'0 auto', textAlign:'center'}}>
        <h2 style={{fontSize:34, fontWeight:700, color:'#111', marginBottom:12}}>Your career intelligence dashboard</h2>
        <p style={{color:'#666', marginBottom:48, fontSize:16}}>Everything in one place — match scores, skill gaps, job listings and your learning roadmap.</p>
        <div style={{background:'white', borderRadius:20, border:'1px solid #eee', padding:36, textAlign:'left'}}>
          <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:24, paddingBottom:16, borderBottom:'1px solid #f0f0f0'}}>
            <div style={{display:'flex', alignItems:'center', gap:8}}>
              <img src="/logo.svg" width="32" height="32" style={{borderRadius:8, flexShrink:0}} alt="SkillSync AI"/>
              <span style={{fontWeight:600, fontSize:14, color:'#111'}}>SkillSync AI</span>
            </div>
            <span style={{fontSize:12, color:'#999'}}>Dashboard preview</span>
          </div>
          <div style={{display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:16, marginBottom:24}}>
            {[
              {label:'Career match score', value:'--', sub:'After your analysis'},
              {label:'Skills identified', value:'--', sub:'Select your skills'},
              {label:'Job matches', value:'--', sub:'Live after signup'},
            ].map(m => (
              <div key={m.label} style={{background:'#f9f9f9', borderRadius:12, padding:16}}>
                <p style={{fontSize:11, color:'#999', marginBottom:4}}>{m.label}</p>
                <p style={{fontSize:26, fontWeight:700, color:'#185FA5'}}>{m.value}</p>
                <p style={{fontSize:11, color:'#999', marginTop:4}}>{m.sub}</p>
              </div>
            ))}
          </div>
          <div style={{background:'#f0f5fb', borderRadius:12, padding:20, textAlign:'center'}}>
            <p style={{color:'#185FA5', fontSize:14, fontWeight:500, marginBottom:4}}>🎯 Your personalized career analysis appears here</p>
            <p style={{color:'#666', fontSize:13}}>Start your analysis to see career matches, skill gaps, and your learning roadmap</p>
          </div>
        </div>
      </div>
    </section>
  )
}