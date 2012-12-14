package scriptease.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

public class CodeBlockMapper extends StoryAdapter {
	private Map<String, List<CodeBlock>> codeBlocks = new HashMap<String, List<CodeBlock>>();

	@Override
	protected void defaultProcessComplex(ComplexStoryComponent complex) {
		complex.processChildren(this);
	}

	@Override
	public void processScriptIt(ScriptIt scriptIt) {
		for (CodeBlock codeBlock : scriptIt.getCodeBlocks()) {
			final String slot;
			final KnowIt subject;
			final String key;

			slot = codeBlock.getSlot();
			subject = codeBlock.getSubject();
			key = subject.getBinding().toString() + slot;

			List<CodeBlock> bucket = this.codeBlocks.get(key);
			if (bucket == null) {
				bucket = new ArrayList<CodeBlock>();
			}

			bucket.add(codeBlock);
			this.codeBlocks.put(key, bucket);
		}
		this.defaultProcessComplex(scriptIt);
	}

	public Map<String, List<CodeBlock>> getCodeBlocks() {
		return this.codeBlocks;
	}
}
