# Design Decision Index

This is a quick-reference index for the decision numbers cited in Table 6 (Task C's traceability table) and elsewhere in this report. Each one is a short summary — the full reasoning behind each is already explained in prose in Task A and Task B where it first comes up; this appendix exists only so a decision number can be looked up on its own, without needing a separate document.

1. Login is a precondition on every use case, not a chained `<<include>>` from Login to all seventeen.
2. Check Dentist Availability is a mandatory `<<include>>` of Register New Appointment, not optional — guarantees the double-booking check can never be skipped.
3. Register New Patient `<<extend>>`s Register New Appointment, triggered only when the patient is new.
4. Administrator generalises Receptionist rather than standing as an unrelated actor.
5. View Reports and Manage Staff Accounts are Administrator-only.
6. Cancel Appointment and Record Appointment Delay both `<<include>>` Search Appointment, since both act on an existing record.
7. One Cancel Appointment use case regardless of who caused the cancellation — the cause is a reason recorded on the appointment, not a separate use case.
8. A Dentist can record their own delay directly but cannot Cancel Appointment (front-desk only, since cancellation has billing consequences).
9. Dentist added as a third actor, beyond the original brief, as explicitly-permitted extra functionality.
10. A dentist-caused delay cascades automatically to that dentist's remaining appointments that day — no decision step, since the dentist is the constrained resource.
11. A patient-caused delay requires a manual Wait/Skip decision, recorded at the time.
12. Reschedule Appointment is its own use case, `<<extend>>`ing Record Appointment Delay only when the outcome is Skip.
13. Set Daily Appointment Limit and Set Availability belong to Dentist and Administrator, not Receptionist.
14. No `<<include>>`/`<<extend>>` drawn between the two settings use cases and Check Dentist Availability — a use case diagram models who triggers what, not data dependencies between use cases.
15. The class hierarchy mirrors the actor generalisation from the use case diagram.
16. Login/logout are defined once on the abstract `User` class, not repeated per subclass.
17. Every appointment state-change is a method on `Appointment` itself, not on `Receptionist`/`Dentist`.
18. Two separate delay methods (`recordDentistDelay`, `recordPatientDelay`) instead of one method with an extra parameter, since the two flows genuinely differ.
19. Three different relationship strengths (aggregation/composition/association) chosen deliberately per pair, not defaulted to plain association.
20. Navigability is one-directional throughout, consistently pointing toward `Appointment`.
21. `AppointmentStatus`/`ChangeReason`/`DelayDecision` connect to `Appointment` by dependency, not association — they're attribute types, not owned objects.
22. Sequence diagrams show Servlet → Service → DAO → Database throughout, matching the 3-tier architecture Task B builds.
23. The patient delay's Wait/Skip decision is modelled as a return-then-recall across two HTTP requests, not one blocking call.
24. `Treatment` carries a default `durationMinutes`, feeding the availability clash check; a full waiting-room/queue subsystem is deliberately not modelled.
25. Persistence uses one `users` table with a `role` discriminator, plus a `dentists` extension table for the columns unique to that subclass.
26. The consultation fee is a flat, clinic-wide amount, not per-dentist or per-treatment — matches how many small clinics actually bill.
27. The flat consultation fee lives in a single-row `clinic_settings` table and gets no class-diagram entry, since no use case gives a user direct control over it.
28. `hasClash()`/`countAppointmentsOnDate()` take an `excludeAppointmentNumber` parameter, added for Reschedule, so an appointment being moved never clashes against its own old slot.
29. `rescheduleAppointment()` re-fetches the dentist via `DentistDAO.findById()` rather than reusing a joined reference, since the joined version's availability blocks are always empty.
30. Every servlet enforces the same actor/use-case boundaries the use case diagram already specified (Register/Search/Bill are Receptionist-or-Administrator only; a Dentist reaches Delay/Reschedule but never Cancel, and only for their own appointments).
31. The `Notification` audit trail gets no class-diagram entry, for the same reasoning as decision 27; `NotificationChannel` is confirmed as the project's one implemented Strategy pattern.
