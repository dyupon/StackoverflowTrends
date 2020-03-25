const EPSILON = 0.00000001;
export class GraphBuilder {
    constructor(inputs) {
        this.inputs = inputs;
        this.topics = [];
        this.tags = [];
        this.tagToInputs = new Map();
        const tags = new Map();
        const topics = new Map();
        this.inputs.forEach(input => {
            const tag = input.tag;
            tags.set(tag, tag);
            topics.set(input.topic, input.topic);
            if (this.tagToInputs.has(tag)) {
                this.tagToInputs.get(tag).push(input);
            }
            else {
                this.tagToInputs.set(tag, [input]);
            }
        });
        this.tags = Array.from(tags.keys());
        this.topics = Array.from(topics.keys());
    }
    getGraph() {
        if (!this.graph) {
            this.graph = {
                nodes: this.createNodes(),
                links: this.createLinks()
            };
        }
        return this.graph;
    }
    createNodes() {
        return this.topics.map(name => ({
            name,
            tags: this.inputs.filter(input => input.topic === name)
                .map(input => input.tag)
        }));
    }
    createLinks() {
        const idToLink = new Map();
        this.tags.forEach(tag => {
            const inputs = this.tagToInputs.get(tag)
                .sort((a, b) => {
                if (Math.abs(a.propensity - b.propensity) < EPSILON) {
                    return 0;
                }
                else {
                    return a.propensity - b.propensity < 0
                        ? 1
                        : -1;
                }
            });
            inputs.forEach((parent, index) => {
                if (index !== inputs.length - 1) {
                    const child = inputs[index + 1];
                    const linkId = `${parent.topic}_${child.topic}`;
                    idToLink.set(linkId, {
                        from: parent.topic,
                        to: child.topic
                    });
                }
            });
        });
        return Array.from(idToLink.values());
    }
}
//# sourceMappingURL=graph-builder.js.map