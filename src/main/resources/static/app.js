// every tab of the dashboard
//   columns   : what the table shows        [json path, label]
//   form      : the input boxes of the form  [json path, label, rule]   rule: "admin" = only the admin sees it,
//                                                                       "new" = only when adding, "admin new" = both
//   password  : the form also has a password box (the 3 people tabs)
//   ownerRole : which role owns rows here     ownerPath: where the owner's username is in a row
//   adders    : which roles may add new rows
var TABS = {
    admins: {
        columns: [["name", "Name"], ["email", "Email"], ["username", "Username"]],
        form: [["name", "Name"], ["email", "Email"], ["username", "Username"]],
        password: true, ownerRole: "ADMIN", ownerPath: "username", adders: ["ADMIN"]
    },
    teachers: {
        columns: [["name", "Name"], ["email", "Email"], ["department", "Department"], ["designation", "Designation"], ["username", "Username"]],
        form: [["name", "Name"], ["email", "Email"], ["department", "Department"], ["designation", "Designation"], ["username", "Username"]],
        password: true, ownerRole: "TEACHER", ownerPath: "username", adders: ["ADMIN"]
    },
    students: {
        columns: [["name", "Name"], ["email", "Email"], ["roll", "Roll"], ["department", "Department"], ["username", "Username"]],
        form: [["name", "Name"], ["email", "Email"], ["roll", "Roll"], ["department", "Department"], ["username", "Username"]],
        password: true, ownerRole: "STUDENT", ownerPath: "username", adders: ["ADMIN"]
    },
    courses: {
        columns: [["code", "Code"], ["title", "Title"], ["credit", "Credit"], ["semester", "Semester"], ["teacher.name", "Teacher"]],
        form: [["code", "Course code"], ["title", "Title"], ["credit", "Credit"], ["semester", "Semester"], ["teacher.username", "Teacher username", "admin"]],
        password: false, ownerRole: "TEACHER", ownerPath: "teacher.username", adders: ["ADMIN", "TEACHER"]
    },
    registrations: {
        columns: [["course.code", "Course"], ["course.title", "Title"], ["course.teacher.name", "Teacher"], ["student.name", "Student"], ["student.roll", "Roll"], ["section", "Section"]],
        form: [["course.code", "Course code", "new"], ["student.username", "Student username", "admin new"], ["section", "Section"]],
        password: false, ownerRole: "STUDENT", ownerPath: "student.username", adders: ["ADMIN", "STUDENT"]
    }
};

// which tabs each role can open
var ALLOWED = {
    ADMIN: ["admins", "teachers", "students", "courses", "registrations"],
    TEACHER: ["teachers", "courses", "registrations"],
    STUDENT: ["students", "courses", "registrations"]
};

var me = null;        // the logged-in user, for example {username: "teacher1", role: "TEACHER"}
var current = "";     // the open tab, for example "teachers"
var items = [];       // the rows loaded from the server
var editing = null;   // the row being edited (null = adding a new one)

// when the page opens: ask who is logged in, then make the tabs
fetch("/api/me").then(function (res) {
    return res.json();
}).then(function (data) {
    me = data;
    document.getElementById("me").innerText = me.username + " (" + me.role + ")";

    var html = "";
    ALLOWED[me.role].forEach(function (tab) {
        html += "<button id='tab-" + tab + "' onclick=\"openTab('" + tab + "')\">" + tabName(tab) + "</button> ";
    });
    document.getElementById("tabs").innerHTML = html;

    openTab(ALLOWED[me.role][0]);
});

function isAdmin() {
    return me.role == "ADMIN";
}

// true if this row belongs to the logged-in user (their account, their course, their registration)
function isMine(item) {
    var tab = TABS[current];
    return tab.ownerRole == me.role && getValue(item, tab.ownerPath) == me.username;
}

// true if the logged-in user may add rows in the open tab
function canAdd() {
    return TABS[current].adders.indexOf(me.role) >= 0;
}

function tabName(tab) {
    if (tab == "registrations" && me.role == "STUDENT") return "My Registrations";
    return capital(tab);
}

// the grey help line under the title
function hint() {
    if (isAdmin()) return "";
    if (current == "courses" && me.role == "TEACHER") return "All courses. Add your own courses below. You can edit or delete only your own courses (highlighted).";
    if (current == "courses") return "All courses. Click Register to take a course.";
    if (current == "registrations" && me.role == "TEACHER") return "Students registered in your courses (read only).";
    if (current == "registrations") return "Your registered courses. You can change the section or drop a course.";
    return "You can see all " + current + ". You can edit or delete only your own account (highlighted).";
}

function openTab(tab) {
    current = tab;
    document.querySelectorAll("#tabs button").forEach(function (button) { button.className = ""; });
    document.getElementById("tab-" + tab).className = "active";

    var title = "All " + capital(tab);
    if (tab == "registrations" && me.role == "STUDENT") title = "My Registrations";
    if (tab == "registrations" && me.role == "TEACHER") title = "Registrations in My Courses";
    document.getElementById("listTitle").innerText = title;
    document.getElementById("hint").innerText = hint();

    showMessage("", "");
    resetForm();
    loadList();
}

// READ: GET /api/{tab}
function loadList() {
    fetch("/api/" + current).then(function (res) {
        if (!res.ok) {
            showMessage("Could not load the list. Error " + res.status, "error");
            return [];
        }
        return res.json();
    }).then(function (list) {
        items = list;
        var columns = TABS[current].columns;

        var html = "<tr><th>ID</th>";
        columns.forEach(function (c) { html += "<th>" + c[1] + "</th>"; });
        html += "<th>Actions</th></tr>";

        if (list.length == 0) {
            html += "<tr><td colspan='" + (columns.length + 2) + "'>No data yet</td></tr>";
        }

        list.forEach(function (item, i) {
            html += isMine(item) ? "<tr class='mine'>" : "<tr>";
            html += "<td>" + item.id + "</td>";
            columns.forEach(function (c) { html += "<td>" + clean(getValue(item, c[0])) + "</td>"; });
            html += "<td>" + buttons(item, i) + "</td></tr>";
        });

        document.getElementById("table").innerHTML = html;
    });
}

// the buttons this user may use on a row
function buttons(item, i) {
    var mine = isMine(item);
    var canEdit = isAdmin() || mine;
    var canDelete = isAdmin() ? !mine : mine;   // admin: everything except their own account, others: only their own rows

    var html = "";
    if (current == "courses" && me.role == "STUDENT") html += "<button onclick='registerCourse(" + i + ")'>Register</button> ";
    if (canEdit) html += "<button onclick='editRow(" + i + ")'>Edit</button> ";
    if (canDelete) html += "<button onclick='deleteRow(" + i + ")'>" + (current == "registrations" ? "Drop" : "Delete") + "</button> ";
    if (mine && TABS[current].password) html += "<i>(you)</i>";
    return html == "" ? "-" : html;
}

// a form box is shown only if its rule allows it
function showField(field) {
    var rule = field[2] || "";
    if (rule.indexOf("admin") >= 0 && !isAdmin()) return false;
    if (rule.indexOf("new") >= 0 && editing != null) return false;
    return true;
}

// build the input boxes of the form
function buildForm() {
    var html = "";
    TABS[current].form.forEach(function (f) {
        if (showField(f)) {
            html += "<div class='form-row'><label>" + f[1] + "</label><input id='f-" + f[0] + "' autocomplete='off'></div>";
        }
    });
    if (TABS[current].password) {
        html += "<div class='form-row'><label>Password</label><input id='f-password' type='password' autocomplete='new-password'></div>";
    }
    document.getElementById("form").innerHTML = html;
}

// the empty "Add New" form (hidden if this user cannot add here)
function resetForm() {
    editing = null;
    buildForm();
    document.getElementById("formTitle").innerText = "Add New " + singular(current);
    document.getElementById("formBox").style.display = canAdd() ? "block" : "none";
}

// fill the form with a row so it can be changed
function editRow(i) {
    editing = items[i];
    buildForm();

    var title = "Edit " + singular(current) + " (ID " + editing.id + ")";
    if (isMine(editing) && TABS[current].password) title = "Edit My Account";
    if (TABS[current].password) title += " - leave password empty to keep the old one";
    document.getElementById("formTitle").innerText = title;
    document.getElementById("formBox").style.display = "block";

    TABS[current].form.forEach(function (f) {
        if (showField(f)) {
            document.getElementById("f-" + f[0]).value = getValue(editing, f[0]);
        }
    });
}

// CREATE: POST /api/{tab}   UPDATE: PUT /api/{tab}/{id}
function save() {
    var data = {};
    TABS[current].form.forEach(function (f) {
        if (showField(f)) {
            setValue(data, f[0], document.getElementById("f-" + f[0]).value);
        }
    });
    if (TABS[current].password) {
        data.password = document.getElementById("f-password").value;
    }

    var isEdit = editing != null;
    var myAccount = isEdit && isMine(editing) && TABS[current].password;
    var url = isEdit ? "/api/" + current + "/" + editing.id : "/api/" + current;

    fetch(url, {
        method: isEdit ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    }).then(function (res) {
        if (!res.ok) {
            showError(res);
            return;
        }
        // a new username or password only works after logging in again
        if (myAccount && (data.username != me.username || data.password != "")) {
            alert("Your login details were changed. Please log in again.");
            location.href = "/logout";
            return;
        }
        showMessage(isEdit ? "Updated successfully (status " + res.status + ")" : "Added successfully (status " + res.status + ")", "success");
        resetForm();
        loadList();
    });
}

// DELETE: DELETE /api/{tab}/{id}
function deleteRow(i) {
    var item = items[i];
    var myAccount = isMine(item) && TABS[current].password;

    var question = "Delete this " + singular(current).toLowerCase() + " (ID " + item.id + ")?";
    if (current == "courses") question = "Delete course " + item.code + "? Its registrations are deleted too.";
    if (current == "registrations") question = "Drop " + item.course.code + " for " + item.student.name + "?";
    if (myAccount) question = "Delete YOUR OWN account? You will be logged out.";
    if (!confirm(question)) {
        return;
    }

    fetch("/api/" + current + "/" + item.id, { method: "DELETE" }).then(function (res) {
        if (!res.ok) {
            showError(res);
            return;
        }
        if (myAccount) {
            location.href = "/logout";
            return;
        }
        showMessage("Deleted successfully (status " + res.status + ")", "success");
        resetForm();
        loadList();
    });
}

// a student registers for a course: POST /api/registrations
function registerCourse(i) {
    var course = items[i];
    var section = prompt("Register for " + course.code + " - " + course.title + "\nSection (for example A or B):", "A");
    if (section == null) {
        return;
    }

    fetch("/api/registrations", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ course: { code: course.code }, section: section })
    }).then(function (res) {
        if (!res.ok) {
            showError(res);
            return;
        }
        showMessage("Registered for " + course.code + " (status " + res.status + "). See My Registrations.", "success");
    });
}

// ---------- small helpers ----------

// getValue(row, "teacher.name") -> row.teacher.name   (empty text if something is missing)
function getValue(obj, path) {
    var parts = path.split(".");
    for (var k = 0; k < parts.length; k++) {
        if (obj == null) return "";
        obj = obj[parts[k]];
    }
    return obj == null ? "" : obj;
}

// setValue(data, "teacher.username", "t1") -> data.teacher = {username: "t1"}
function setValue(obj, path, value) {
    var parts = path.split(".");
    for (var k = 0; k < parts.length - 1; k++) {
        if (obj[parts[k]] == null) obj[parts[k]] = {};
        obj = obj[parts[k]];
    }
    obj[parts[parts.length - 1]] = value;
}

function showMessage(text, type) {
    var msg = document.getElementById("msg");
    msg.className = type;
    msg.innerText = text;
}

function showError(res) {
    res.text().then(function (text) {
        var error = text;
        try { error = JSON.parse(text).error; } catch (e) { }
        showMessage("Error " + res.status + ": " + error, "error");
    });
}

// "teachers" -> "Teachers"
function capital(word) {
    return word.charAt(0).toUpperCase() + word.slice(1);
}

// "teachers" -> "Teacher"
function singular(word) {
    return capital(word).slice(0, -1);
}

// stop HTML inside the data from running
function clean(value) {
    return String(value).replace(/</g, "&lt;");
}
