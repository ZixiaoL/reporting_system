package com.antra.evaluation.reporting_system.validation;

import javax.validation.GroupSequence;

@GroupSequence({NotNullCheck.class, LogicCheck.class, MultiSheetNotNullCheck.class, MultiSheetLogicCheck.class})
public interface MultiSheetGroupSequences {
}
