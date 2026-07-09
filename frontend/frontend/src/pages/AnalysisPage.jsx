import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import useWindowWidth from '../hooks/useWindowWidth'
import { runAnalysis, uploadResume } from '../api'

const skillCategories = {
  'Engineering & Technology': {
    'Programming Languages': ['Python','Java','C','C++','JavaScript','TypeScript','R','MATLAB','Swift','Kotlin'],
    'Web Development': ['React','Node.js','HTML/CSS','Angular','Vue.js','Django','Flask','REST APIs'],
    'Data & AI': ['Machine Learning','Deep Learning','Data Analysis','SQL','Power BI','Tableau','OpenCV','NLP'],
    'Core Engineering': ['Circuit Design','AutoCAD','SolidWorks','Embedded Systems','IoT','Robotics','VLSI'],
    'Soft Skills': ['Problem Solving', 'Critical Thinking', 'Teamwork', 'Communication', 'Time Management', 'Leadership', 'Adaptability', 'Presentation Skills'],
  },
  'Medical & Health Sciences': {
    'Clinical Skills': ['Patient Assessment','ECG Reading','Clinical Diagnosis','First Aid','Pharmacology','Anatomy'],
    'Research & Lab': ['Lab Techniques','Research Methodology','Medical Writing','Clinical Trials','Pathology'],
    'Healthcare Tech': ['Electronic Health Records','Medical Imaging','Telemedicine','Hospital Management'],
    'Soft Skills': ['Patient Communication','Medical Ethics','Team Collaboration','Leadership'],
    'Professional Skills': ['Empathy', 'Communication', 'Critical Thinking', 'Teamwork', 'Ethics', 'Time Management', 'Stress Management', 'Leadership'],
  },
  'Business & Commerce': {
    'Finance & Accounting': ['Financial Analysis','Tally','GST','Auditing','Taxation','Cost Accounting','Excel'],
    'Management': ['Project Management','Operations','Supply Chain','Business Strategy','Agile / Scrum'],
    'Marketing': ['Digital Marketing','SEO','Content Marketing','Social Media','Brand Management','CRM'],
    'CA / CS Specific': ['Company Law','IFRS','Internal Audit','Risk Management','Financial Reporting'],
    'Soft Skills': ['Negotiation', 'Communication', 'Leadership', 'Critical Thinking', 'Time Management', 'Problem Solving', 'Networking', 'Presentation Skills'],
  },
  'Arts, Design & Media': {
    'Design': ['UI/UX Design','Figma','Adobe XD','Photoshop','Illustrator','Motion Graphics','3D Modelling'],
    'Media & Communication': ['Content Writing','Journalism','Video Editing','Public Relations','Copywriting'],
    'Fine Arts': ['Photography','Animation','Illustration','Typography','Brand Identity'],
    'Performing Arts': ['Music','Acting','Dance','Scriptwriting','Event Management'],
    'Soft Skills': ['Creativity', 'Communication', 'Storytelling', 'Collaboration', 'Time Management', 'Presentation Skills', 'Critical Thinking', 'Adaptability'],
  },
  'Science & Research': {
    'Core Sciences': ['Physics','Chemistry','Biology','Biochemistry','Microbiology','Environmental Science'],
    'Research Skills': ['Research Methodology','Statistical Analysis','SPSS','Academic Writing','Lab Skills'],
    'Applied Sciences': ['Biotechnology','Nanotechnology','Forensic Science','Geoscience','Astronomy'],
    'Soft Skills': ['Research Communication', 'Critical Thinking', 'Teamwork', 'Grant Writing', 'Presentation Skills', 'Time Management', 'Problem Solving'],
  },
  'Law & Social Sciences': {
    'Legal Skills': ['Contract Law','Corporate Law','Criminal Law','Legal Research','Moot Court','Drafting'],
    'Social Sciences': ['Psychology','Sociology','Economics','Political Science','Public Policy'],
    'Communication': ['Public Speaking','Negotiation','Critical Thinking','Debate','Report Writing'],
    'Soft Skills': ['Argumentation', 'Empathy', 'Communication', 'Critical Thinking', 'Leadership', 'Time Management', 'Ethics', 'Negotiation'],
  },
}

const fieldColors = {
  'Engineering & Technology': { bg:'#EEF2FF', border:'#818CF8', active:'#4F46E5', text:'#3730A3' },
  'Medical & Health Sciences': { bg:'#FFF1F2', border:'#FDA4AF', active:'#E11D48', text:'#9F1239' },
  'Business & Commerce': { bg:'#ECFDF5', border:'#6EE7B7', active:'#059669', text:'#065F46' },
  'Arts, Design & Media': { bg:'#FFF7ED', border:'#FDC57B', active:'#D97706', text:'#92400E' },
  'Science & Research': { bg:'#EFF6FF', border:'#93C5FD', active:'#2563EB', text:'#1E3A8A' },
  'Law & Social Sciences': { bg:'#F5F3FF', border:'#C4B5FD', active:'#7C3AED', text:'#4C1D95' },
}

export default function AnalysisPage() {
  const [selectedField, setSelectedField] = useState(null)
  const [selected, setSelected] = useState([])
  const [mode, setMode] = useState('skills')
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const width = useWindowWidth()
  const isMobile = width < 768
  const token = localStorage.getItem('token')

  const toggleSkill = (skill) =>
    setSelected(prev => prev.includes(skill) ? prev.filter(s => s !== skill) : [...prev, skill])

  const handleSubmit = async () => {
    if (mode === 'skills' && selected.length === 0) return
    if (mode === 'resume' && !file) return
    if (!token) {
      localStorage.setItem('pendingField', selectedField)
      localStorage.setItem('pendingSkills', JSON.stringify(selected))
      navigate('/login')
      return
    }
    setLoading(true)
    try {
      let res
      if (mode === 'resume') {
        res = await uploadResume(file)
      } else {
        res = await runAnalysis(selectedField, selected)
      }
      navigate('/results', { state: { results: res } })
    } catch (err) {
      alert('Analysis failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const colors = selectedField ? fieldColors[selectedField] : null

  return (
    <div style={{minHeight:'100vh', background:'#f8f9fa'}}>
      <Navbar />
      <div style={{maxWidth:900, margin:'0 auto', padding: isMobile ? '80px 16px 80px' : '90px 24px 80px'}}>
        <div style={{textAlign:'center', marginBottom:40}}>
          <h1 style={{fontSize:34, fontWeight:700, color:'#111', marginBottom:10}}>Start your career analysis</h1>
          <p style={{color:'#666', fontSize:16, maxWidth:560, margin:'0 auto'}}>
            Select your field of study, pick the skills you know, and let AI map your ideal career path.
          </p>
        </div>

        <div style={{display:'flex', background:'white', borderRadius:12, border:'1px solid #eee', padding:4, width:'fit-content', margin:'0 auto 40px'}}>
          {['skills','resume'].map(m => (
            <button key={m} onClick={() => setMode(m)} style={{padding:'10px 28px', borderRadius:10, border:'none', cursor:'pointer', fontWeight:500, fontSize:14, background: mode===m ? '#185FA5' : 'transparent', color: mode===m ? 'white' : '#666'}}>
              {m === 'skills' ? 'Select skills' : 'Upload resume'}
            </button>
          ))}
        </div>

        {mode === 'resume' ? (
          <div style={{background:'white', borderRadius:20, border:'1px solid #eee', padding:48, textAlign:'center', marginBottom:24}}>
            {!file ? (
              <label style={{cursor:'pointer', display:'block'}}>
                <div style={{width:72, height:72, background:'#E6F1FB', borderRadius:20, display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 16px', fontSize:32}}>📄</div>
                <p style={{fontWeight:600, color:'#111', fontSize:16, marginBottom:6}}>Upload your resume</p>
                <p style={{color:'#999', fontSize:13, marginBottom:20}}>PDF supported · Max 5MB</p>
                <div style={{border:'2px dashed #ddd', borderRadius:16, padding:'32px 24px', display:'inline-block', minWidth:300}}>
                  <p style={{color:'#185FA5', fontWeight:500, fontSize:14}}>Click to browse file</p>
                </div>
                <input type="file" accept=".pdf" style={{display:'none'}} onChange={e => setFile(e.target.files[0])} />
              </label>
            ) : (
              <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', background:'#E6F1FB', borderRadius:12, padding:'16px 24px', maxWidth:400, margin:'0 auto'}}>
                <div style={{textAlign:'left'}}>
                  <p style={{fontSize:14, fontWeight:600, color:'#185FA5'}}>{file.name}</p>
                  <p style={{fontSize:12, color:'#5B9BD5'}}>{(file.size/1024).toFixed(1)} KB · Ready to analyze</p>
                </div>
                <button onClick={() => setFile(null)} style={{background:'none', border:'none', cursor:'pointer', fontSize:20, color:'#185FA5'}}>✕</button>
              </div>
            )}
          </div>
        ) : (
          <>
            <div style={{marginBottom:32}}>
              <div style={{display:'flex', alignItems:'center', gap:10, marginBottom:16}}>
                <div style={{width:28, height:28, background:'#185FA5', borderRadius:'50%', display:'flex', alignItems:'center', justifyContent:'center', color:'white', fontSize:13, fontWeight:700}}>1</div>
                <h2 style={{fontSize:17, fontWeight:600, color:'#111'}}>Select your field of study</h2>
              </div>
              <div style={{display:'grid', gridTemplateColumns: isMobile ? 'repeat(2, 1fr)' : 'repeat(auto-fit, minmax(200px, 1fr))', gap:12}}>
                {Object.keys(skillCategories).map(field => {
                  const c = fieldColors[field]
                  const isActive = selectedField === field
                  return (
                    <button key={field} onClick={() => { setSelectedField(field); setSelected([]) }}
                      style={{padding:'16px 20px', borderRadius:14, border: isActive ? `2px solid ${c.active}` : `1px solid #eee`, background: isActive ? c.bg : 'white', cursor:'pointer', textAlign:'left'}}>
                      <p style={{fontWeight:600, fontSize:14, color: isActive ? c.active : '#333', marginBottom:2}}>{field}</p>
                      <p style={{fontSize:12, color: isActive ? c.text : '#999'}}>{Object.keys(skillCategories[field]).length} categories</p>
                    </button>
                  )
                })}
              </div>
            </div>

            {selectedField && (
              <div style={{marginBottom:24}}>
                <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:16}}>
                  <div style={{display:'flex', alignItems:'center', gap:10}}>
                    <div style={{width:28, height:28, background:'#185FA5', borderRadius:'50%', display:'flex', alignItems:'center', justifyContent:'center', color:'white', fontSize:13, fontWeight:700}}>2</div>
                    <h2 style={{fontSize:17, fontWeight:600, color:'#111'}}>
                      Select your skills in <span style={{color: colors.active}}>{selectedField}</span>
                    </h2>
                  </div>
                  {selected.length > 0 && (
                    <span style={{fontSize:13, color:'white', background:colors.active, padding:'4px 12px', borderRadius:20, fontWeight:500}}>
                      {selected.length} selected
                    </span>
                  )}
                </div>
                <div style={{display:'flex', flexDirection:'column', gap:20}}>
                  {Object.entries(skillCategories[selectedField]).map(([category, skills]) => (
                    <div key={category} style={{background:'white', borderRadius:16, border:'1px solid #eee', padding:24}}>
                      <h3 style={{fontSize:14, fontWeight:600, color:'#333', marginBottom:14, paddingBottom:10, borderBottom:`2px solid ${colors.bg}`, display:'flex', alignItems:'center', gap:8}}>
                        <span style={{width:8, height:8, borderRadius:'50%', background:colors.active, display:'inline-block'}}></span>
                        {category}
                      </h3>
                      <div style={{display:'flex', flexWrap:'wrap', gap:8}}>
                        {skills.map(skill => {
                          const isSelected = selected.includes(skill)
                          return (
                            <button key={skill} onClick={() => toggleSkill(skill)}
                              style={{padding:isMobile ? '6px 12px' : '8px 16px', borderRadius:10, border: isSelected ? `1.5px solid ${colors.active}` : '1px solid #e5e5e5', background: isSelected ? colors.bg : 'white', color: isSelected ? colors.active : '#555', fontSize:13, fontWeight: isSelected ? 600 : 400, cursor:'pointer'}}>
                              {skill}
                            </button>
                          )
                        })}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {!selectedField && (
              <div style={{background:'white', borderRadius:16, border:'2px dashed #eee', padding:48, textAlign:'center', marginBottom:24}}>
                <p style={{fontSize:28, marginBottom:12}}>👆</p>
                <p style={{color:'#999', fontSize:15}}>Select your field above to see relevant skills</p>
              </div>
            )}
          </>
        )}

        {/* Login banner — shows when not logged in and skills are selected */}
        {!token && selected.length > 0 && (
          <div style={{background:'#FFF7ED', border:'1px solid #FDC57B', borderRadius:12, padding:'14px 20px', marginBottom:12, display:'flex', alignItems:'center', justifyContent:'space-between', flexWrap:'wrap', gap:10}}>
            <p style={{fontSize:14, color:'#92400E', fontWeight:500, margin:0}}>
              🔒 Login required to see your career results
            </p>
            <button
              onClick={() => {
                localStorage.setItem('pendingField', selectedField)
                localStorage.setItem('pendingSkills', JSON.stringify(selected))
                navigate('/login')
              }}
              style={{background:'#185FA5', color:'white', border:'none', padding:'8px 20px', borderRadius:8, fontSize:13, fontWeight:600, cursor:'pointer', whiteSpace:'nowrap'}}>
              Log in for results →
            </button>
          </div>
        )}

        {/* Main analyze button — redirects to login if not logged in */}
        <button
          onClick={handleSubmit}
          disabled={loading || (mode==='skills' ? selected.length===0 : !file)}
          style={{width:'100%', background: !token && (mode==='skills' ? selected.length > 0 : file) ? '#0d4a87' : '#185FA5', color:'white', border:'none', padding:isMobile ? '16px' : '18px', borderRadius:14, fontSize:16, fontWeight:600, cursor:'pointer', opacity: loading || (mode==='skills' ? selected.length===0 : !file) ? 0.4 : 1, marginTop:8}}>
          {loading ? 'Analyzing... please wait ⏳' :
           !token && (mode==='skills' ? selected.length > 0 : file) ? 'Log in to see results →' :
           selected.length > 0 ? `Analyze my ${selected.length} skills →` : 'Analyze my profile'}
        </button>

        {selected.length > 0 && (
          <p style={{textAlign:'center', fontSize:13, color:'#999', marginTop:10}}>
            Selected: {selected.slice(0,5).join(', ')}{selected.length > 5 ? ` +${selected.length-5} more` : ''}
          </p>
        )}
      </div>
    </div>
  )
}