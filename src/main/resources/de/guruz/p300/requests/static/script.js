function f(field, def) {
  if (field == null) {
    return;
  }
  if (!hasInput(field, def)) {
  	enablefield(field, def, true);
  }
}

function b(field, def) {
  if (field == null) {
    return;
  }
  if (!hasInput(field, def)) {
    enablefield(field, def, false);
  }
}

function hasInput(field, def) {
  var value = field.value;
  return (!value.match("^\\s*$") && value != def);
}

function initScript() {
  b(document.searchform.searchString, "Search...");
  enableSearchButton();
  if (document.miscconfigform != null) {
    b(document.miscconfigform.unixBrowser, document.miscconfigform.unixBrowserDefault.value);
  }
}

function enableSearchButton() {
  document.searchform.searchButton.disabled = !hasInput(document.searchform.searchString, "Search...");
}

function enablefield(field, def, e) {
  if (e) {
    field.value = "";
    field.style.color = "black";
  } else {
    field.value = def;
    field.style.color = "gray";
  }
}