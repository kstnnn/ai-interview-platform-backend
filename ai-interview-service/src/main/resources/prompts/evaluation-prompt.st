You are evaluating a candidate's answer in a technical interview.

Question:
{question}

Expected answer:
{expected_answer}

Candidate answer:
{candidate_answer}

Evaluation rules:
1. Compare the candidate answer only against the expected answer and the question asked.
2. Do not reward unrelated information.
3. Do not require exact wording if the meaning is correct.
4. Penalize confident but incorrect statements.
5. If the answer is empty, evasive, or unrelated, give low scores and set shouldAskFollowUp to true.
6. Ask for a follow-up only when one focused clarification could reasonably reveal whether the candidate understands the missing concept.
7. Do not reveal the full expected answer in candidateFeedback.

Score each category from 0.0 to 1.0:
- correctnessScore: factual correctness and coverage of key points.
- depthScore: understanding of why/how the concept works.
- practicalScore: ability to apply the concept in real scenarios.

Set totalScore as the rounded average of correctnessScore, depthScore, and practicalScore.
Set confidence from 0.0 to 1.0 based on how clear the candidate answer is.

Output valid JSON only. Do not wrap it in Markdown. Do not add commentary outside JSON.

JSON schema:
{{
  "correctnessScore": 0.7,
  "depthScore": 0.5,
  "practicalScore": 0.6,
  "totalScore": 0.6,
  "confidence": 0.8,
  "knowledgeGaps": ["missing or weak concept"],
  "strengths": ["correctly covered concept"],
  "shouldAskFollowUp": true,
  "followUpFocus": "one specific concept to clarify, or null",
  "candidateFeedback": "Brief feedback for the candidate, 1-2 sentences max."
}}
