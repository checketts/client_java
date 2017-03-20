package io.prometheus.client.parser;

import io.prometheus.client.Collector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class TextParser {

    /**
     * Parse Prometheus text format from a string.
     *
     * @param input Input to parse
     * @return List of MetricFamilySamples
     */
    public List<Collector.MetricFamilySamples> parse(String input) {
        return parse(new BufferedReader(new StringReader(input)));
    }

    public List<Collector.MetricFamilySamples> parse(File inputFile) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(inputFile));
            return parse(br);
        } catch (FileNotFoundException e) {
            throw new ParseException("Error parsing file "+inputFile.toString(), e);
        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new ParseException("Error closing file", e);
                }
            }
        }
    }

    private List<Collector.MetricFamilySamples> parse(BufferedReader reader) {
        List<Collector.MetricFamilySamples> samples = new ArrayList<Collector.MetricFamilySamples>();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                // process the line.
            }
        } catch (IOException e) {
            throw new ParseException("Error parsing file", e);
        }

        return samples;
    }


//
//
//    def _parse_sample(text):
//    name = []
//    labelname = []
//    labelvalue = []
//    value = []
//    labels = {}
//
//    state = 'name'
//
//            for char in text:
//            if state == 'name':
//            if char == '{':
//    state = 'startoflabelname'
//    elif char == ' ' or char == '\t':
//    state = 'endofname'
//            else:
//            name.append(char)
//    elif state == 'endofname':
//            if char == ' ' or char == '\t':
//    pass
//    elif char == '{':
//    state = 'startoflabelname'
//            else:
//            value.append(char)
//    state = 'value'
//    elif state == 'startoflabelname':
//            if char == ' ' or char == '\t' or char == ',':
//    pass
//    elif char == '}':
//    state = 'endoflabels'
//            else:
//    state = 'labelname'
//            labelname.append(char)
//    elif state == 'labelname':
//            if char == '=':
//    state = 'labelvaluequote'
//    elif char == ' ' or char == '\t':
//    state = 'labelvalueequals'
//            else:
//            labelname.append(char)
//    elif state == 'labelvalueequals':
//            if char == '=':
//    state = 'labelvaluequote'
//    elif char == ' ' or char == '\t':
//    pass
//            else:
//    raise ValueError("Invalid line: " + text)
//    elif state == 'labelvaluequote':
//            if char == '"':
//    state = 'labelvalue'
//    elif char == ' ' or char == '\t':
//    pass
//            else:
//    raise ValueError("Invalid line: " + text)
//    elif state == 'labelvalue':
//            if char == '\\':
//    state = 'labelvalueslash'
//    elif char == '"':
//    labels[''.join(labelname)] = ''.join(labelvalue)
//    labelname = []
//    labelvalue = []
//    state = 'nextlabel'
//            else:
//            labelvalue.append(char)
//    elif state == 'labelvalueslash':
//    state = 'labelvalue'
//            if char == '\\':
//            labelvalue.append('\\')
//    elif char == 'n':
//            labelvalue.append('\n')
//    elif char == '"':
//            labelvalue.append('"')
//            else:
//            labelvalue.append('\\' + char)
//    elif state == 'nextlabel':
//            if char == ',':
//    state = 'startoflabelname'
//    elif char == '}':
//    state = 'endoflabels'
//    elif char == ' ' or char == '\t':
//    pass
//            else:
//    raise ValueError("Invalid line: " + text)
//    elif state == 'endoflabels':
//            if char == ' ' or char == '\t':
//    pass
//            else:
//                    value.append(char)
//    state = 'value'
//    elif state == 'value':
//            if char == ' ' or char == '\t':
//            # Timestamps are not supported, halt
//                break
//                        else:
//                        value.append(char)
//            return (''.join(name), labels, float(''.join(value)))
//
//
//    def text_fd_to_metric_families(fd):
//            """Parse Prometheus text format from a file descriptor.
//    This is a laxer parser than the main Go parser,
//    so successful parsing does not imply that the parsed
//    text meets the specification.
//    Yields core.Metric's.
//            """
//    name = ''
//    documentation = ''
//    typ = 'untyped'
//    samples = []
//    allowed_names = []
//
//    def build_metric(name, documentation, typ, samples):
//    metric = core.Metric(name, documentation, typ)
//    metric.samples = samples
//        return metric
//
//    for line in fd:
//    line = line.strip()
//
//            if line.startswith('#'):
//    parts = line.split(None, 3)
//            if len(parts) < 2:
//            continue
//            if parts[1] == 'HELP':
//            if parts[2] != name:
//            if name != '':
//    yield build_metric(name, documentation, typ, samples)
//                    # New metric
//    name = parts[2]
//    typ = 'untyped'
//    samples = []
//    allowed_names = [parts[2]]
//            if len(parts) == 4:
//    documentation = _unescape_help(parts[3])
//                else:
//    documentation = ''
//    elif parts[1] == 'TYPE':
//            if parts[2] != name:
//            if name != '':
//    yield build_metric(name, documentation, typ, samples)
//                    # New metric
//    name = parts[2]
//    documentation = ''
//    samples = []
//    typ = parts[3]
//    allowed_names = {
//        'counter': [''],
//        'gauge': [''],
//        'summary': ['_count', '_sum', ''],
//        'histogram': ['_count', '_sum', '_bucket'],
//    }.get(typ, [''])
//    allowed_names = [name + n for n in allowed_names]
//            else:
//            # Ignore other comment tokens
//    pass
//    elif line == '':
//            # Ignore blank lines
//            pass
//        else:
//    sample = _parse_sample(line)
//            if sample[0] not in allowed_names:
//            if name != '':
//    yield build_metric(name, documentation, typ, samples)
//                  # New metric, yield immediately as untyped singleton
//    name = ''
//    documentation = ''
//    typ = 'untyped'
//    samples = []
//    allowed_names = []
//    yield build_metric(sample[0], documentation, typ, [sample])
//            else:
//                    samples.append(sample)
//
//            if name != '':
//    yield build_metric(name, documentation, typ, samples)

}




//Not sure why needed
//////////////////////////////////////////////////////////////////////////////

//    def _unescape_help(text):
//    result = []
//    slash = False
//
//    for char in text:
//            if slash:
//            if char == '\\':
//            result.append('\\')
//    elif char == 'n':
//            result.append('\n')
//            else:
//            result.append('\\' + char)
//    slash = False
//        else:
//                if char == '\\':
//    slash = True
//          else:
//                  result.append(char)
//
//            if slash:
//            result.append('\\')
//
//            return ''.join(result)
