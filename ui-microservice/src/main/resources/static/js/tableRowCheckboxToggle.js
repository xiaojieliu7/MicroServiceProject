/**********************************************************************/
/* jQuery Plugin: tableRowCheckboxToggle
/* Written by: Dodo http://pure-essence.net
/* Written for jQuery v1.2.3 on Feb 26, 2008
/* Version 1.0
/**********************************************************************/
// give me a list of table rows classes you want to apply this affect to
var tableRowCheckboxToggleClasses=new Array('odd_row', 'even_row');
// give me a class name you want to add to the table row when there are checkboxes checked
var tableRowCheckboxCheckedClass='marked';
// by default, this script will apply to checkboxes within the rows specified
// HOWEVER, you may manually exclude certain checkboxes based on their name, id or class
// specify below
var excludeCheckboxesWithNames=new Array('testName');
var excludeCheckboxesWithIds=new Array('checkme100', 'checkme101');
var excludeCheckboxesWithClasses=new Array('testClass');



/**********************************************************************/
/* STOP editing unless you know what you are doing :)
/**********************************************************************/
// on page load, this script goes thru all tr elements
// prepare the form when the DOM is ready 
$(document).ready(function() {
	// array used to remember the checkbox state for the checkboxes applicable
	var tableRowCheckboxes = new Array;
	// traverse all rows
	$("tr").each(function(i,row) {
		// do something to the rows with the particular classes specified
		$.each(tableRowCheckboxToggleClasses, function(j,tableRowCheckboxToggleClass) {
			if($(row).hasClass(tableRowCheckboxToggleClass)) {
				hasChecked = false;

				$(row).click(function() {
					// toggle the checked state
					$("tr:eq("+i+") :checkbox").each(function(j,checkbox) {
						if(applicableCheckbox(checkbox)) {
							uniqueId = '' + i + j;
							// toggle checkbox states
							if (typeof(tableRowCheckboxes[uniqueId]) == 'undefined' || !tableRowCheckboxes[uniqueId]) {
								$(row).addClass(tableRowCheckboxCheckedClass);
								tableRowCheckboxes[uniqueId] = true;
							} else {
								$(row).removeClass(tableRowCheckboxCheckedClass);
								tableRowCheckboxes[uniqueId] = false;
							}
							checkbox.checked = tableRowCheckboxes[uniqueId];
						}// end of if applicable checkbox
					});
				}); // end of click event
				
				// for initialization
				$("tr:eq("+i+") :checkbox").each(function(j,checkbox) {
					if(applicableCheckbox(checkbox) && checkbox.checked) {
						hasChecked = true;
						return false;
					}
				});

				// if the row contains checked applicable checkboxes, mark all checkboxes within row as checked in memory
				// and mark the row
				if(hasChecked) {
					$("tr:eq("+i+") :checkbox").each(function(j,checkbox) {
						if(applicableCheckbox(checkbox)) {
							uniqueId = '' + i + j;
							$(row).addClass(tableRowCheckboxCheckedClass);
							tableRowCheckboxes[uniqueId] = true;
						}
					});
				}

			} // end of if the tr has the applicable class
		});
	});
}); // end of DOM ready

function applicableCheckbox(checkbox) {
	var applicable = true;
	// not applicable if the checkbox is disabled
	if(checkbox.disabled) {
		applicable = false;
	} else {
		$.each(excludeCheckboxesWithNames, function(a,excludeCheckboxesWithName) {
			if(jQuery.trim(checkbox.name) == jQuery.trim(excludeCheckboxesWithName)) {
				applicable = false;
			}
		});
		$.each(excludeCheckboxesWithIds, function(b,excludeCheckboxesWithId) {
			if(jQuery.trim(checkbox.id) == jQuery.trim(excludeCheckboxesWithId)) {
				applicable = false;
			}
		});
		$.each(excludeCheckboxesWithClasses, function(c,excludeCheckboxesWithClass) {
			if($(checkbox).hasClass(jQuery.trim(excludeCheckboxesWithClass))) {
				applicable = false;
			}
		});
	}
	return applicable;
}