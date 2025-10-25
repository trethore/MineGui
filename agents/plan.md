# Planning & Brainstorming

Use this playbook whenever you need to explore approaches or scope a task before coding.

## Purpose
- Capture assumptions, open questions, ideas and possible solutions for completing the task.
- Keep planning lightweight so you can iterate quickly and archive once work begins.
- Share enough detail that another contributor can pick up the task mid-plan if needed.

## Template
When drafting or updating `plan.txt`, mirror this structure:

```
# [Feature or Task Title]

## Steps
- [ ] Step 1: Describe the first actionable milestone
- [ ] Step 2: Note a concrete follow-up
- [ ] Step 3: Capture any optional stretch goals

## Description
[Brief paragraph outlining goals, constraints, and the reasoning behind the approach.]

## Ideas & Alternatives
- Potential improvement or variant 1
- Alternative approach worth revisiting 2
- Future enhancement to consider 3

## Questions
- [ ] Question for the user or stakeholder about X
- [ ] Clarification needed on Y
```

- Use `- [ ]` to track pending work and `- [x]` once a step or question is resolved.
- Add or remove sections if the task demands more (e.g., risk assessment, dependencies).

## Workflow
1. **Before planning**: If `plan.txt` already contains content, confirm with the user before clearing or archiving it.
2. **During planning**: Populate each section with concrete details; flag unknowns in the Questions list.
3. **During implementation**: Update checkboxes as you complete steps or answer questions.
4. **After completion**: Summarize outcomes, archive or clear `plan.txt`, and link any follow-ups in the main conversation.

## Tips
- Before writing a plan, try to understand how the codebase currently handles related concerns
- Keep plans concise—favor bullet points over long paragraphs.
- Link back to relevant specs, issues, or design discussions so context remains discoverable.
- Note explicit trade-offs when selecting between approaches to inform future iterations.
