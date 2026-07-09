DELETE FROM career_mapping;

INSERT INTO career_mapping (career_title, required_skills, description, is_active) VALUES
('Product Manager', 'Communication,Leadership,Project Management,Agile/Scrum,Research,Problem Solving,Business Strategy,Data Analysis,Excel', 'Leads product vision and roadmap across teams', true),
('Data Analyst', 'Python,SQL,Data Analysis,Excel,Statistical Analysis,Power BI,Tableau,Research Methodology,Problem Solving', 'Turns raw data into actionable business insights', true),
('Software Engineer', 'Python,JavaScript,React,Node.js,SQL,Problem Solving,REST APIs,HTML/CSS,Java', 'Builds and maintains software applications', true),
('UX Designer', 'UI/UX Design,Figma,Adobe XD,Research,Communication,Problem Solving,Photoshop,Illustration,User Testing', 'Designs intuitive user experiences and interfaces', true),
('Digital Marketer', 'Digital Marketing,SEO,Content Writing,Social Media,Marketing,Brand Management,CRM,Content Marketing,Copywriting', 'Drives online growth through digital channels', true),
('Data Scientist', 'Python,Machine Learning,Deep Learning,SQL,Statistical Analysis,Data Analysis,NLP,Tableau,Research Methodology', 'Builds ML models to solve complex business problems', true),
('Business Analyst', 'Data Analysis,Excel,Business Strategy,Communication,Problem Solving,SQL,Project Management,Research,Agile/Scrum', 'Bridges business needs and technical solutions', true),
('Financial Analyst', 'Financial Analysis,Excel,Tally,GST,Taxation,Cost Accounting,Financial Reporting,Auditing,Risk Management', 'Analyzes financial data to guide business decisions', true),
('Doctor/Clinician', 'Patient Assessment,Clinical Diagnosis,Anatomy,Pharmacology,Medical Ethics,First Aid,ECG Reading,Patient Communication,Lab Techniques', 'Diagnoses and treats patients in clinical settings', true),
('Medical Researcher', 'Research Methodology,Lab Techniques,Medical Writing,Clinical Trials,Pathology,Statistical Analysis,Academic Writing,Biology,Biochemistry', 'Conducts research to advance medical knowledge', true),
('Healthcare Administrator', 'Hospital Management,Electronic Health Records,Telemedicine,Leadership,Communication,Project Management,Operations,Medical Ethics', 'Manages healthcare facility operations', true),
('CA/Accountant', 'Financial Analysis,Tally,GST,Auditing,Taxation,Cost Accounting,Company Law,IFRS,Internal Audit,Financial Reporting', 'Manages financial records and ensures compliance', true),
('Legal Associate', 'Contract Law,Legal Research,Drafting,Corporate Law,Communication,Criminal Law,Moot Court,Report Writing,Critical Thinking', 'Provides legal counsel and handles cases', true),
('Content Writer', 'Content Writing,Research,Communication,SEO,Academic Writing,Journalism,Copywriting,Public Speaking,Social Media', 'Creates engaging written content for various platforms', true),
('Biotechnologist', 'Biotechnology,Lab Techniques,Research Methodology,Biology,Biochemistry,Microbiology,Academic Writing,Lab Skills', 'Applies biological systems to develop products', true),
('Environmental Scientist', 'Environmental Science,Research Methodology,Statistical Analysis,Lab Skills,Academic Writing,Geoscience,Biology', 'Studies environmental systems and sustainability', true),
('Psychologist', 'Psychology,Communication,Research Methodology,Statistical Analysis,Academic Writing,Critical Thinking,Public Speaking', 'Studies human behavior and provides mental health support', true),
('Economist', 'Economics,Statistical Analysis,Research Methodology,Data Analysis,Excel,Financial Analysis,Public Policy,Critical Thinking', 'Analyzes economic trends and advises on policy', true),
('Graphic Designer', 'Photoshop,Illustrator,Figma,Typography,Brand Identity,Motion Graphics,Adobe XD,Photography,Animation', 'Creates visual content for brands and media', true),
('Full Stack Developer', 'React,Node.js,JavaScript,Python,SQL,REST APIs,HTML/CSS,Problem Solving,Java', 'Builds complete web applications front to back', true);
