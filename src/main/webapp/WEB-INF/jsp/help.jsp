<%-- pageEncoding declared here, not just in the included header.jsp — see
     manage-appointment.jsp's comment on the same line for why. --%>
<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Help — Sunrise Dental Clinic"); %>
<%@ include file="/WEB-INF/jspf/header.jsp" %>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Help — Step-by-Step Guide for New Staff</h1>

    <div class="card stack">
        <div>
            <h2 style="margin-top:0;">1. Logging in</h2>
            <p>Use the username and password given to you by the clinic administrator.
               Your session stays active for 30 minutes of inactivity, after which
               you'll be asked to log in again.</p>
        </div>

        <div>
            <h2>2. Registering a new appointment</h2>
            <p>From the Dashboard, choose "Register New Appointment". Enter the
               patient's name, address, and contact number, then pick a dentist,
               a treatment, and a date/time. If that contact number already
               belongs to an existing patient, their record is reused automatically
               — you don't need to search for them first. The system checks the
               dentist's availability (existing bookings, blocked-out periods, and
               their daily appointment limit) before confirming; if the slot isn't
               available you'll be asked to choose a different time.</p>
        </div>

        <div>
            <h2>3. Finding an appointment</h2>
            <p>Choose "Display Appointment Details" and enter the appointment
               number (shown on the confirmation screen after booking, and on any
               printed bill). This shows the full record — patient, dentist,
               treatment, date/time, and current status.</p>
        </div>

        <div>
            <h2>4. Billing a patient</h2>
            <p>From a found appointment, choose "Calculate &amp; Print Bill" (or
               enter the appointment number directly from the Dashboard's Bill
               option). The total is the treatment cost plus the clinic's
               consultation fee. Use the Print button to print or save the
               receipt from your browser.</p>
        </div>

        <div>
            <h2>5. Cancelling, recording a delay, or rescheduling</h2>
            <p>From a found appointment, choose "Cancel / Delay / Reschedule".
               Three things you can do there:</p>
            <ul>
                <li><strong>Cancel</strong> — pick whether the patient or the
                    dentist caused it.</li>
                <li><strong>Record a delay</strong> — if the <em>dentist</em> is
                    running late, every one of their remaining appointments that
                    day shifts automatically. If a <em>patient</em> is running
                    late, you're asked whether the clinic will wait (which shifts
                    the rest of the day, same as above) or the patient is being
                    skipped (which cancels that slot — reschedule them below once
                    a new time is agreed).</li>
                <li><strong>Reschedule</strong> — move an appointment to a new
                    date/time; the same availability checks as booking apply.</li>
            </ul>
        </div>

        <div>
            <h2>6. Logging out / ending your shift</h2>
            <p>Use "Log out" at the top of the page when you're done. This
               ends your session — always log out before leaving your workstation
               unattended.</p>
        </div>
    </div>
<%@ include file="/WEB-INF/jspf/footer.jsp" %>
