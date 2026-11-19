/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
// The pages of a RIFE2 site inside an app, included by the context:nativeAppScript
// value of a template. A page does here what the app's web view doesn't do by
// itself: it marks itself, and asks the app for printing, and on Android also
// for sharing, saving a file and what a page posts. An app of Apple is asked
// through the "rife2" message handler of its web view, an Android app through
// the Rife2NativeApp object that it puts in the site's own pages, which
// takes messages and answers some.
//
// A page and its app also tell each other what's their own, through
// window.rife2NativeApp: tell(name, data) tells the app, which it only
// hears when hears(name) says so, and on(name, heard) hears what the app
// tells the page. The app says what it hears, and which protocol it speaks,
// in window.rife2NativeAppHost before the page's scripts run, or when an older
// web view of Android can't, through the told() of window.Rife2NativeAppHost.
(() => {
    function told(bridge) {
        const waiting = new Map();
        let number = 0;
        bridge.addEventListener('message', event => {
            const said = JSON.parse(event.data);
            const answer = waiting.get(said.id);
            if (answer) {
                waiting.delete(said.id);
                answer(said.name);
            }
        });
        const tell = said => bridge.postMessage(JSON.stringify(said));
        return {
            print: () => tell({action: 'print'}),
            share: (title, url) => tell({action: 'share', title: title, url: url}),
            save: (name, type, data) => tell({action: 'save', name: name, type: type, data: data}),
            posted: (type, data) => new Promise(answer => {
                const id = ++number;
                waiting.set(id, answer);
                tell({action: 'posted', id: id, type: type, data: data});
            }),
            tell: (name, data) => tell({action: 'tell', name: name, data: data})
        };
    }

    const platform = document.currentScript ? String(document.currentScript.dataset.platform || '').toLowerCase() : '';
    if (!/^[a-z0-9-]+$/.test(platform || '') || window.rife2NativeApp) {
        return;
    }
    const apple = window.webkit && window.webkit.messageHandlers ? window.webkit.messageHandlers.rife2 : null;
    const android = window.Rife2NativeApp ? told(window.Rife2NativeApp) : null;
    // an app of before the protocol says nothing, and hears nothing of a page's own; an
    // older web view of Android has the app say it when it's asked
    let host = window.rife2NativeAppHost;
    if (!host && window.Rife2NativeAppHost && typeof window.Rife2NativeAppHost.told === 'function') {
        try {
            host = JSON.parse(window.Rife2NativeAppHost.told());
        } catch (e) {
            host = null;
        }
    }
    host = host && typeof host === 'object' ? host : {};

    // the styles can have rules for a page of an app, and for one platform
    document.documentElement.classList.add('in-app', 'on-' + platform);

    // A page keeps its size under two fingers and a double tap, as a screen of
    // an app does, and is laid out down to the edges of the screen, unless
    // the app says that its pages zoom. An Android app says so with the
    // settings of its web view.
    const viewport = document.querySelector('meta[name=viewport]');
    if (apple && viewport && host.zooms !== true) {
        viewport.setAttribute('content', 'width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, viewport-fit=cover');
    }

    // a web view has no print dialog of its own
    if (apple) {
        window.print = () => apple.postMessage({action: 'print'});
    } else if (android) {
        window.print = () => android.print();
    }

    if (android) {
        androidOnly(android);
    }

    const listeners = new Map();
    const hears = name => Array.isArray(host.heard) && host.heard.includes(String(name));
    // once all of the above is in place, which a second inclusion leaves alone
    window.rife2NativeApp = Object.freeze({
        platform: platform,
        protocol: Number(host.protocol) || 0,
        // whether the app hears what a page tells it by that name
        hears: hears,
        // tells the app something that a script can send, answers whether it was told
        tell: (name, data) => {
            if (!hears(name)) {
                return false;
            }
            const said = {action: 'tell', name: String(name), data: data === undefined ? null : data};
            try {
                if (apple) {
                    apple.postMessage(said);
                } else if (android) {
                    android.tell(said.name, said.data);
                } else {
                    return false;
                }
            } catch (e) {
                // what a message can't carry, a function or an element for instance
                return false;
            }
            return true;
        },
        // hears what the app tells the page by that name, the one listener before it no longer
        on: (name, listener) => {
            if (typeof listener === 'function') {
                listeners.set(String(name), listener);
            } else {
                listeners.delete(String(name));
            }
        },
        // what the app tells the page, answers whether it was heard
        told: (name, data) => {
            const listener = listeners.get(String(name));
            if (!listener) {
                return false;
            }
            listener(data);
            return true;
        }
    });

    // what a web view of Android lacks beside printing
    function androidOnly(android) {
        navigator.share = shared => {
            android.share(String(shared.title || ''), String(shared.url || shared.text || ''));
            return Promise.resolve();
        };

        // a page saves a file with a link that names the file, clicked by a script or by a finger
        const save = link => {
            const name = link.download;
            fetch(link.href).then(answer => answer.blob()).then(blob => {
                const reader = new FileReader();
                reader.onload = () => android.save(name, blob.type, reader.result.slice(reader.result.indexOf(',') + 1));
                reader.readAsDataURL(blob);
            });
        };
        const click = HTMLAnchorElement.prototype.click;
        HTMLAnchorElement.prototype.click = function() {
            if (this.hasAttribute('download')) {
                save(this);
            } else {
                click.call(this);
            }
        };
        addEventListener('click', event => {
            const link = event.target.closest ? event.target.closest('a[download]') : null;
            if (link && !event.defaultPrevented) {
                event.preventDefault();
                save(link);
            }
        });

        // A web view of Android asks the app for a page without what was
        // posted to it. So the app is handed that first, and the request says
        // which it was: in a header when a script asks, in its address when a
        // form does, which the page that comes back takes out of its own
        // address again. What isn't covered goes without what it posts: a
        // request that a script waits for, navigator.sendBeacon, a worker,
        // and a form that posts as plain text.
        const here = new URL(location.href);
        if (here.searchParams.has('rife2-body')) {
            here.searchParams.delete('rife2-body');
            history.replaceState(history.state, '', here.href);
        }
        // the scheme and the host, since an origin of a scheme of an app's own can be null for every address
        const own = address => {
            const to = new URL(address, location.href);
            return to.protocol === location.protocol && to.host === location.host;
        };
        // hands the app what a request posts, as what kind when the request
        // itself doesn't say, and answers what names it
        const handed = async (asked, type) => {
            const bytes = new Uint8Array(await asked.arrayBuffer());
            let text = '';
            for (let at = 0; at < bytes.length; at += 32768) {
                text += String.fromCharCode.apply(null, bytes.subarray(at, at + 32768));
            }
            return android.posted(type, btoa(text));
        };

        const fetched = window.fetch;
        window.fetch = async function(input, init) {
            const asked = new Request(input, init);
            if (asked.method === 'GET' || asked.method === 'HEAD' || !own(asked.url)) {
                return fetched.call(this, asked);
            }
            let named;
            try {
                named = await handed(asked.clone(), '');
            } catch (e) {
                // the request still goes, without what it posts
                return fetched.call(this, asked);
            }
            const headers = new Headers(asked.headers);
            headers.set('X-Rife2-Body', named);
            return fetched.call(this, new Request(asked, {headers: headers}));
        };

        const open = XMLHttpRequest.prototype.open, send = XMLHttpRequest.prototype.send, header = XMLHttpRequest.prototype.setRequestHeader,
            abort = XMLHttpRequest.prototype.abort;
        XMLHttpRequest.prototype.open = function(method, address, later) {
            // one that a script waits for can't hand anything over first
            this.rife2Hands = (arguments.length < 3 || !!later) && own(address);
            this.rife2Typed = false;
            this.rife2Opened = (this.rife2Opened || 0) + 1;
            return open.apply(this, arguments);
        };
        XMLHttpRequest.prototype.abort = function() {
            this.rife2Opened = (this.rife2Opened || 0) + 1;
            return abort.apply(this, arguments);
        };
        XMLHttpRequest.prototype.setRequestHeader = function(name) {
            this.rife2Typed = this.rife2Typed || String(name).toLowerCase() === 'content-type';
            return header.apply(this, arguments);
        };
        XMLHttpRequest.prototype.send = function(body) {
            if (body == null || body instanceof Document || !this.rife2Hands) {
                return send.call(this, body);
            }
            const asked = new Request(location.href, {method: 'POST', body: body});
            const opened = this.rife2Opened;
            // unless it was given up or opened again meanwhile
            const going = () => this.rife2Opened === opened && this.readyState === XMLHttpRequest.OPENED;
            handed(asked, this.rife2Typed ? '' : asked.headers.get('content-type') || '').then(posted => {
                if (going()) {
                    header.call(this, 'X-Rife2-Body', posted);
                    send.call(this, body);
                }
            }, () => {
                if (going()) {
                    send.call(this, body);
                }
            });
        };

        // of a form, or of the button that submits it when the button says
        // otherwise; through the form's own properties, which a field of the
        // same name hides
        const property = what => Object.getOwnPropertyDescriptor(HTMLFormElement.prototype, what);
        const says = (form, button, what) => button && button.hasAttribute('form' + what) ? button['form' + what[0].toUpperCase() + what.slice(1)] : property(what).get.call(form);
        const posts = (form, button) => says(form, button, 'method') === 'post' && own(says(form, button, 'action'))
            && ['', '_self'].includes(says(form, button, 'target')) && says(form, button, 'enctype') !== 'text/plain';
        const submit = HTMLFormElement.prototype.submit;
        const post = async (form, button) => {
            const filled = button ? new FormData(form, button) : new FormData(form);
            const asked = new Request(location.href, {method: 'POST', body: says(form, button, 'enctype') === 'multipart/form-data' ? filled : new URLSearchParams(filled)});
            const to = new URL(says(form, button, 'action'), location.href);
            try {
                to.searchParams.set('rife2-body', await handed(asked, asked.headers.get('content-type') || ''));
            } catch (e) {
                // the form still goes, without what it posts
            }
            // what the button said, since the form is submitted without it,
            // and then what the form said, for a post that leaves the page
            // where it is, like a file that's saved
            const said = ['action', 'method', 'enctype'].map(what => [what, Element.prototype.getAttribute.call(form, what)]);
            property('action').set.call(form, to.href);
            property('method').set.call(form, says(form, button, 'method'));
            property('enctype').set.call(form, says(form, button, 'enctype'));
            submit.call(form);
            for (const [what, value] of said) {
                if (null === value) {
                    Element.prototype.removeAttribute.call(form, what);
                } else {
                    Element.prototype.setAttribute.call(form, what, value);
                }
            }
        };
        addEventListener('submit', event => {
            // only what really submits: an event that a script made up doesn't
            if (event.isTrusted && !event.defaultPrevented && posts(event.target, event.submitter)) {
                event.preventDefault();
                post(event.target, event.submitter);
            }
        });
        HTMLFormElement.prototype.submit = function() {
            if (posts(this)) {
                post(this);
            } else {
                submit.call(this);
            }
        };
    }
})();
