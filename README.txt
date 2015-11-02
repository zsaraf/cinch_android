Major remaining to-do items:
- hook up scheduling to the rest of the request flow -- right now it's just a visual interface, with commited blocks representing the current
list of available blocks.
- drag handling doesn't work for when a user drags horizontally (meaning a block should span more than one day column)
- fair amount of bug testing
- the "NOW" line that's in the ios version was previously implemented, but then we changed the way we draw columns, so now it's deprecated -- initCurrentTime needs to be rewritten

Known Bugs:
- there's a kind of unavoidable bug (might only be on Nexus 5) where, for some reason, certain ACTION_UP touch events don't register.  Basically,
if you really fuck with the scheduling view and do super unusual tapping behavior, it can possibly trigger the bug, meaning the view will think the user is starting to drag when he'd only tapped -- so the user would just see the shadow of a single block, even though he's not holding or dragging.  The only way I found of mitigating this is to just make sure that a new ACTION_DOWN touch event will cancel the dragging.  There might be a solution to this, but frankly it shouldn't happen under normal user behavior.
