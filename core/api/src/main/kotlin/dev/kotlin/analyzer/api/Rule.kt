package dev.kotlin.analyzer.api

import dev.kotlin.analyzer.domain.RuleDescriptor

public interface Rule {
    public val descriptor: RuleDescriptor
}

public interface PsiRule : Rule {
    public fun visitPsi(file: AnalyzedFile, context: PsiRuleContext)
}

public interface SemanticRule : Rule {
    public fun visitSemantic(file: AnalyzedFile, session: AnalysisSession, context: SemanticRuleContext)
}
